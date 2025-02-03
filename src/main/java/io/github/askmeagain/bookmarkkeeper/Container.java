package io.github.askmeagain.bookmarkkeeper;

import com.intellij.ide.bookmark.Bookmark;
import com.intellij.ide.bookmark.BookmarkType;

public class Container {

  final BookmarkType type;
  final Bookmark bookmark;
  final String groupName;

  Container(BookmarkType type, Bookmark bookmark, String groupName) {
    this.type = type;
    this.bookmark = bookmark;
    this.groupName = groupName;
  }
}