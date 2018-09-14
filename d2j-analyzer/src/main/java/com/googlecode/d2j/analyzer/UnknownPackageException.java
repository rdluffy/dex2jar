package com.googlecode.d2j.analyzer;

public class UnknownPackageException extends Exception {
    String p;

    public UnknownPackageException(String p) {
        super(p);
        this.p = p;
    }
}
