package in.oneton.idea.spring.assistant.plugin.suggestion.component;

import com.intellij.openapi.compiler.CompilationStatusListener;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import in.oneton.idea.spring.assistant.plugin.suggestion.service.SuggestionService;
import org.jetbrains.annotations.NotNull;

public class BootstrapListener implements CompilationStatusListener {

    private static final Logger log = Logger.getInstance(BootstrapListener.class);

    @Override
    public void compilationFinished(final boolean aborted, final int errors, final int warnings, @NotNull final CompileContext compileContext) {
        Project project = compileContext.getProject();
        SuggestionService service = project.getService(SuggestionService.class);
        debug(() -> log
                .debug("Received compilation status event for project " + project.getName()));
        if (errors == 0) {
            CompileScope projectCompileScope = compileContext.getProjectCompileScope();
            CompileScope compileScope = compileContext.getCompileScope();
            if (projectCompileScope == compileScope) {
                service.reIndex(project);
            } else {
                service.reindex(project, compileContext.getCompileScope().getAffectedModules());
            }
            debug(() -> log.debug("Compilation status processed for project " + project.getName()));
        } else {
            debug(() -> log
                    .debug("Skipping reindexing completely as there are " + errors + " errors"));
        }
    }

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

}
