/**
* Copyright Search Technologies 2011
*/
package com.searchtechnologies.aspire.docprocessing;

public class NameValuePair {
  NameValuePair(String name, String value) {
    this.name = name.trim();
    this.value = value.trim();
  }
  String name;
  String value;
}