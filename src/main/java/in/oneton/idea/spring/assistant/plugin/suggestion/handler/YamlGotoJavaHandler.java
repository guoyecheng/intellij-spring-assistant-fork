package in.oneton.idea.spring.assistant.plugin.suggestion.handler;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import in.oneton.idea.spring.assistant.plugin.suggestion.OriginalNameProvider;
import in.oneton.idea.spring.assistant.plugin.suggestion.Suggestion;
import in.oneton.idea.spring.assistant.plugin.suggestion.completion.FileType;
import in.oneton.idea.spring.assistant.plugin.suggestion.metadata.MetadataPropertySuggestionNode;
import in.oneton.idea.spring.assistant.plugin.suggestion.metadata.json.SpringConfigurationMetadataProperty;
import in.oneton.idea.spring.assistant.plugin.suggestion.service.SuggestionService;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLPsiElement;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.intellij.openapi.module.ModuleUtilCore.findModuleForPsiElement;

public class YamlGotoJavaHandler  implements GotoDeclarationHandler {

    public static final PsiElement[] DEFAULT_RESULT = new PsiElement[0];

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable final PsiElement sourceElement, final int offset, final Editor editor) {
        if (sourceElement != null && sourceElement.getLanguage().is(YAMLLanguage.INSTANCE)) {
            return gotoJava(sourceElement);
        }
        return DEFAULT_RESULT;
    }

    private PsiElement[] gotoJava(PsiElement sourceElement) {
        YAMLPsiElement yamlPsiElement = PsiTreeUtil.getParentOfType(sourceElement, YAMLPsiElement.class);
        if(yamlPsiElement == null){
            return DEFAULT_RESULT;
        }
        String configFullName = YAMLUtil.getConfigFullName(yamlPsiElement);
        String[] configNameSplit = configFullName.split("\\.");
        Pair<PsiElement, String> value = YAMLUtil.getValue((YAMLFile) sourceElement.getContainingFile(), configNameSplit);
        if (Objects.isNull(value) || !(value.getFirst() instanceof YAMLPlainTextImpl)) {
            return DEFAULT_RESULT;
        }
        List<PsiElement> result = new ArrayList<>();

        result.addAll(findByPropertiesClass(sourceElement, configFullName));

        return result.toArray(DEFAULT_RESULT);
    }

    private List<PsiElement> findByPropertiesClass(final PsiElement sourceElement, String configFullName){
        String[] configNameSplit = configFullName.split("\\.");
        Module module = findModuleForPsiElement(sourceElement);
        Project project = sourceElement.getProject();

        List<PsiElement> result = new ArrayList<>();

        SuggestionService suggestionService = project.getService(SuggestionService.class);
        List<LookupElementBuilder> suggestionsForQueryPrefix = suggestionService.findSuggestionsForQueryPrefix(project, module, FileType.yaml, sourceElement, null, configFullName, null);
        if(suggestionsForQueryPrefix != null ) for (LookupElementBuilder lookupElementBuilder : suggestionsForQueryPrefix) {
            if(lookupElementBuilder.getObject() instanceof Suggestion){
                Suggestion suggestion = (Suggestion) lookupElementBuilder.getObject();
                List<? extends OriginalNameProvider> matchesForReplacement = suggestion.getMatchesForReplacement();
                JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                if(! matchesForReplacement.isEmpty()) {
                    OriginalNameProvider originalNameProvider = matchesForReplacement.get(matchesForReplacement.size() - 1);
                    if(originalNameProvider instanceof MetadataPropertySuggestionNode){
                        SpringConfigurationMetadataProperty property = ((MetadataPropertySuggestionNode) originalNameProvider).getProperty();

                        // find has "sourceType" property
                        String sourceType = property.getSourceType();
                        String name = configNameSplit[configNameSplit.length - 1];
                        if(name.isEmpty()){
                            continue;
                        }
                        if(sourceType == null){
                            continue;
                        }

                        PsiClass psiClass = javaPsiFacade.findClass(sourceType, GlobalSearchScope.allScope(project));
                        if(psiClass == null){
                            continue;
                        }

                        // find field
                        PsiField psiField = psiClass.findFieldByName(name, true);
                        if(psiField != null){
                            result.add(psiField);
                            break;
                        }
                        // find setter and getter
                        {
                            String setter= "set" + name.substring(0,1).toUpperCase() + name.substring(1);
                            PsiMethod[] methodsByName = psiClass.findMethodsByName(setter, true);
                            if(methodsByName.length > 0){
                                result.add(methodsByName[0]);
                                break;
                            }
                        }
                        {
                            String getter= "get" + name.substring(0,1).toUpperCase() + name.substring(1);
                            PsiMethod[] methodsByName = psiClass.findMethodsByName(getter, true);
                            if(methodsByName.length > 0){
                                result.add(methodsByName[0]);
                                break;
                            }
                        }
                        // non found field, direct use class
                        result.add(psiClass);
                    }
                }
            }
        }

        return result;
    }
}
