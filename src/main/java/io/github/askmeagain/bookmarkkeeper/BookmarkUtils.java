package io.github.askmeagain.bookmarkkeeper;

import com.intellij.ide.bookmark.FileBookmark;
import com.intellij.openapi.vfs.VfsUtilCore;
import io.github.askmeagain.bookmarkkeeper.dtos.BookmarkContainer;

import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class BookmarkUtils {

  private BookmarkUtils() {
  }

  public static String bookmarkKey(BookmarkContainer container) {
    var attributes = container.bookmark == null ? null : container.bookmark.getAttributes();
    var attributesKey = attributes == null ? "" : attributes.entrySet().stream()
        .sorted(Entry.comparingByKey())
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining("|"));

    var bookmarkText = container.bookmark == null ? "" : container.bookmark.toString();
    var type = container.type == null ? "" : container.type.toString();
    var groupName = container.groupName == null ? "" : container.groupName;
    var description = container.description == null ? "" : container.description;

    return groupName + "||" + type + "||" + description + "||" + attributesKey + "||" + bookmarkText;
  }

  public static boolean bookmarkExists(BookmarkContainer container) {
    if (container.bookmark instanceof FileBookmark fileBookmark) {
      var file = fileBookmark.getFile();

      return VfsUtilCore.virtualToIoFile(file).exists();
    }

    return true;
  }
}
