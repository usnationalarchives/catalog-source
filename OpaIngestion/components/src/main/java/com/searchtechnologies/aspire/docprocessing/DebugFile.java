/**
* Copyright Search Technologies 2011
*/
package com.searchtechnologies.aspire.docprocessing;

import java.io.OutputStream;

/** A pool of pointers to debug output files. This is so that multiple threads 
 * can all write to multiple files simultaneously without stepping on each other.
 * @author Paul Nelson
 *
 */
public class DebugFile {
  DebugFile(String baseName, String extension, OutputStream os, String fullName) {
    this.baseName = baseName;
    this.extension = extension;
    this.os = os;
    this.fullName = fullName;
  }

  String fullName;
  String baseName;
  String extension;
  OutputStream os;
}
