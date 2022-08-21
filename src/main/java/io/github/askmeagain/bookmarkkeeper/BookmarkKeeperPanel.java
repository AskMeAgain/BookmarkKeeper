package io.github.askmeagain.bookmarkkeeper;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.ui.TaskDialogPanel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class BookmarkKeeperPanel extends TaskDialogPanel {

  private final Project project;
  private final JCheckBox keepBookmarksCheckbox = new JCheckBox("Keep Bookmarks");

  public BookmarkKeeperPanel(Project project) {
    this.project = project;
    keepBookmarksCheckbox.setSelected(true);

    BookmarkKeeperManager.getInstance().saveBookmarks(project);
  }

  @NotNull
  @Override
  public JComponent getPanel() {

    var titledBorder = IdeBorderFactory.createTitledBorder("Bookmarks", false);

    var panel = FormBuilder.createFormBuilder()
        .addComponent(keepBookmarksCheckbox)
        .getPanel();
    panel.setBorder(titledBorder);
    titledBorder.acceptMinimumSize(panel);

    return panel;
  }

  @Override
  public void commit() {
    if (keepBookmarksCheckbox.isSelected()) {
      ApplicationManager.getApplication().invokeLater(() -> BookmarkKeeperManager.getInstance().loadBookmarks(project));
    }
  }
}