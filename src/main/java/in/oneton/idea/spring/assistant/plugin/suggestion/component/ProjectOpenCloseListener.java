// Copyright 2000-2021 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package in.oneton.idea.spring.assistant.plugin.suggestion.component;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import in.oneton.idea.spring.assistant.plugin.suggestion.service.SuggestionService;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ProjectOpenCloseListener implements ProjectManagerListener {

    private static final Logger log = Logger.getInstance(ProjectOpenCloseListener.class);
  /**
   * Invoked on project open.
   *
   * @param project opening project
   */
  @Override
  public void projectOpened(@NotNull Project project) {
    // Ensure this isn't part of testing
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return;
    }
    // Get the counting service
      SuggestionService projectCountingService =
              project.getService(SuggestionService.class);
    // Increment the project count
      try {
          projectCountingService.init(project);
      } catch (IOException e) {
          log.error(e);
      }
  }

  /**
   * Invoked on project close.
   *
   * @param project closing project
   */
  @Override
  public void projectClosed(@NotNull Project project) {
  }

}
