package in.oneton.idea.spring.assistant.plugin.suggestion.component;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import in.oneton.idea.spring.assistant.plugin.suggestion.service.SuggestionService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenImportListener;
import org.jetbrains.idea.maven.project.MavenProject;

import java.util.Collection;
import java.util.List;

import static in.oneton.idea.spring.assistant.plugin.misc.GenericUtil.moduleNamesAsStrCommaDelimited;

public class MavenReIndexingDependencyChangeSubscriberListener
        implements MavenImportListener {

    private static final Logger log =
            Logger.getInstance(MavenReIndexingDependencyChangeSubscriberListener.class);



    /**
     * Debug logging can be enabled by adding fully classified class name/package name with # prefix
     * For eg., to enable debug logging, go `Help > Debug log settings` & type `#in.oneton.idea.spring.assistant.plugin.suggestion.service.SuggestionServiceImpl`
     *
     * @param doWhenDebug code to execute when debug is enabled
     */
    private void debug(Runnable doWhenDebug) {
        if (log.isDebugEnabled()) {
            doWhenDebug.run();
        }
    }


    @Override
    public void importFinished(@NotNull final Collection<MavenProject> importedProjects, @NotNull final List<Module> newModules) {
        for (Module newModule : newModules) {
            Project project = newModule.getProject();
            SuggestionService service = project.getService(SuggestionService.class);
            boolean proceed = importedProjects.stream().anyMatch(
                    _project -> project.getName().equals(_project.getName()) && _project.getDirectory()
                            .equals(project.getBasePath()));
            if (proceed) {
                debug(() -> log.debug("Maven dependencies are updated for project " + project.getName()));


                DumbService.getInstance(project).smartInvokeLater(() -> {
                    log.debug("Will attempt to trigger indexing for project " + project.getName());
                    try {
                        Module[] modules = ModuleManager.getInstance(project).getModules();
                        if (modules.length > 0) {
                            service.reindex(project, modules);
                        } else {
                            debug(() -> log.debug("Skipping indexing for project " + project.getName()
                                    + " as there are no modules"));
                        }
                    } catch (Throwable e) {
                        log.error("Error occurred while indexing project " + project.getName() + " & modules "
                                + moduleNamesAsStrCommaDelimited(newModules, false), e);
                    }
                });
            } else {
                log.debug(
                        "Skipping indexing as none of the imported projects match our project " + project
                                .getName());
            }
        }
    }
}
