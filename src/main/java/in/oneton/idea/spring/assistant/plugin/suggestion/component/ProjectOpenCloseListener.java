//// Copyright 2000-2021 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
//
//package in.oneton.idea.spring.assistant.plugin.suggestion.component;
//
//import com.intellij.lang.xml.XMLLanguage;
//import com.intellij.openapi.application.ApplicationManager;
//import com.intellij.openapi.command.WriteCommandAction;
//import com.intellij.openapi.diagnostic.Logger;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.project.ProjectManagerListener;
//import com.intellij.openapi.vfs.VirtualFile;
//import com.intellij.psi.PsiManager;
//import com.intellij.psi.SingleRootFileViewProvider;
//import com.intellij.psi.impl.source.xml.XmlFileImpl;
//import com.intellij.psi.tree.IFileElementType;
//import com.intellij.psi.xml.XmlAttribute;
//import com.intellij.psi.xml.XmlDocument;
//import com.intellij.psi.xml.XmlTag;
//import in.oneton.idea.spring.assistant.plugin.suggestion.service.SuggestionService;
//import org.jetbrains.annotations.NotNull;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Objects;
//import java.util.Set;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//public class ProjectOpenCloseListener implements ProjectManagerListener {
//
//    private static final Logger log = Logger.getInstance(ProjectOpenCloseListener.class);
//
//    public static String[] FRAMEWORK_METHOD_ANNOTATIONS = new String[]{
//            "org.springframework.web.bind.annotation.GetMapping",
//            "org.springframework.web.bind.annotation.PostMapping",
//            "org.springframework.web.bind.annotation.PutMapping",
//            "org.springframework.web.bind.annotation.DeleteMapping",
//            "org.springframework.web.bind.annotation.RequestMapping",
//            "org.springframework.beans.factory.annotation.Value",
//            "org.springframework.beans.factory.annotation.Autowired",
//            "org.springframework.beans.factory.annotation.Qualifier",
//            "org.springframework.context.annotation.Bean",
//            "javax.annotation.PostConstruct",
//            "javax.annotation.PreDestroy",
//            "javax.annotation.Resource",
//            "javax.annotation.Resources",
//            "org.springframework.scheduling.annotation.Scheduled"
//    };
//
//    public static String[] FRAMEWORK_FIELD_ANNOTATIONS = new String[]{
//            "org.springframework.beans.factory.annotation.Value",
//            "org.springframework.beans.factory.annotation.Autowired",
//            "org.springframework.beans.factory.annotation.Qualifier",
//            "javax.annotation.Resource",
//            "javax.annotation.Resources",
//    };
//
//    /**
//     * Invoked on project open.
//     *
//     * @param project opening project
//     */
//    @Override
//    public void projectOpened(@NotNull Project project) {
//        // Ensure this isn't part of testing
//        if (ApplicationManager.getApplication().isUnitTestMode()) {
//            return;
//        }
//        // Get the counting service
//        SuggestionService projectCountingService =
//                project.getService(SuggestionService.class);
//        // Increment the project count
//        try {
//            projectCountingService.init(project);
//        } catch (IOException e) {
//            log.error(e);
//        }
//
//        //put annotations config
//        try {
//            putAnnotationsConfig(project);
//        } catch (Exception e) {
//            log.error(e);
//        }
//    }
//
//    private void putAnnotationsConfig(Project project) {
//        VirtualFile projectFile = project.getProjectFile();
//        if (projectFile == null) {
//            return;
//        }
//        if(!"misc.xml".equals(projectFile.getName())){
//            return;
//        }
//        PsiManager psiManager = project.getService(PsiManager.class);
//        SingleRootFileViewProvider singleRootFileViewProvider = new SingleRootFileViewProvider(psiManager, projectFile);
//        XmlFileImpl xmlFile = new XmlFileImpl(singleRootFileViewProvider, new IFileElementType(XMLLanguage.INSTANCE));
//
//
//        WriteCommandAction.writeCommandAction(project, xmlFile).compute(() -> {
//            XmlDocument document = xmlFile.getDocument();
//            if (document == null) {
//                return null;
//            }
//            XmlTag rootTag = document.getRootTag();
//            XmlTag entryPointsManager = Stream.of(rootTag.findSubTags("component"))
//                    .filter(tag -> {
//                        XmlAttribute name = tag.getAttribute("name");
//                        return name != null && Objects.equals(name.getValue(), "EntryPointsManager");
//                    })
//                    .findFirst()
//                    .orElse(null);
//            boolean entryPointsManagerIsNew = entryPointsManager == null;
//            if (entryPointsManagerIsNew) {
//                entryPointsManager = rootTag.createChildTag("component", rootTag.getNamespace(), "", false);
//                entryPointsManager.setAttribute("name", "EntryPointsManager");
//            }
//
//            XmlTag list = entryPointsManager.findFirstSubTag("list");
//            boolean listIsNew = list == null;
//            if (listIsNew) {
//                list = entryPointsManager.createChildTag("list", entryPointsManager.getNamespace(), "", false);
//            }
//            {
//                Set<String> existItems = Stream.of(list.getSubTags())
//                        .filter($ -> "item".equals($.getName()))
//                        .map($ -> $.getAttribute("itemvalue"))
//                        .filter(Objects::nonNull)
//                        .map(XmlAttribute::getValue)
//                        .collect(Collectors.toSet());
//                List<String> addItems = Stream.of(FRAMEWORK_METHOD_ANNOTATIONS)
//                        .filter($ -> !existItems.contains($))
//                        .collect(Collectors.toList());
//                for (int i = 0; i < addItems.size(); i++) {
//                    String frameworkAnnotation = addItems.get(i);
//                    XmlTag xmlTag = list.createChildTag("item", list.getNamespace(), "", false);
//                    xmlTag.setAttribute("index", String.valueOf(i + existItems.size()));
//                    xmlTag.setAttribute("class", "java.lang.String");
//                    xmlTag.setAttribute("itemvalue", frameworkAnnotation);
//                    list.addSubTag(xmlTag, false);
//                }
//                list.setAttribute("size", String.valueOf(existItems.size() + addItems.size()));
//            }
//
//            XmlTag writeAnnotations = entryPointsManager.findFirstSubTag("writeAnnotations");
//            boolean writeAnnotationsisNew = writeAnnotations == null;
//            if (writeAnnotationsisNew) {
//                writeAnnotations = entryPointsManager.createChildTag("writeAnnotations", entryPointsManager.getNamespace(), "", false);
//            }
//            {
//                Set<String> existItems = Stream.of(writeAnnotations.getSubTags())
//                        .filter($ -> "writeAnnotation".equals($.getName()))
//                        .map($ -> $.getAttribute("name"))
//                        .filter(Objects::nonNull)
//                        .map(XmlAttribute::getValue)
//                        .collect(Collectors.toSet());
//                List<String> addItems = Stream.of(FRAMEWORK_FIELD_ANNOTATIONS)
//                        .filter($ -> !existItems.contains($))
//                        .collect(Collectors.toList());
//                for (String addItem : addItems) {
//                    XmlTag writeAnnotation = writeAnnotations.createChildTag("writeAnnotation", writeAnnotations.getNamespace(), "", false);
//                    writeAnnotation.setAttribute("name", addItem);
//                    writeAnnotations.addSubTag(writeAnnotation, false);
//                }
//            }
//
//            if (listIsNew) {
//                entryPointsManager.addSubTag(list, false);
//            }
//            if (writeAnnotationsisNew) {
//                entryPointsManager.addSubTag(writeAnnotations, false);
//            }
//            if (entryPointsManagerIsNew) {
//                rootTag.addSubTag(entryPointsManager, false);
//            }
//
//            log.info("putAnnotationsConfig: project = [" + project + "], file = [" + projectFile + "]");
//            return null;
//        });
//
//    }
//
//    /**
//     * Invoked on project close.
//     *
//     * @param project closing project
//     */
//    @Override
//    public void projectClosed(@NotNull Project project) {
//    }
//
//}
