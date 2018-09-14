package com.googlecode.d2j.analyzer;

import java.util.*;

public class Statistics {
    DBPackage dbPackage = new DBPackage();
    private long nbTypes=0;
    private long nbUnknownTypes=0;
    private long nbKnownTypes=0;
    private long nbObfuscatedTypeName = 0;
    private double percentageObfuscatedTypeName=0;
    private double percentageObfuscatedTypeNameOnUnknownType=0;
    Set<String> knownLibs = new HashSet<>();

    public long getNbTypes() {
        return nbTypes;
    }

    public void addType(String type){
        nbTypes++;
        String [] token = type.split("/");
        token[0] = token[0].substring(1);

        String p = token[0];
        for (int i = 1; i < token.length - 1; i++) {
            p+="/"+token[i];
        }
        try {
            String k = dbPackage.getLib(p);
            knownLibs.add(k);


            nbKnownTypes++;
        } catch (UnknownPackageException e) {
            nbUnknownTypes++;
            System.out.println(Arrays.toString(token));
        }
        analyzeType(token);
    }

    private void analyzeType(String [] token){
        if(token[token.length-1].length()<3 ||
                (
                token[token.length-1].length()<4 &&
                token[token.length-1].contains("$"))    ){
            nbObfuscatedTypeName++;
        }
    }
    public void finalize(){
        percentageObfuscatedTypeName = ((double)nbObfuscatedTypeName)/nbTypes;
        percentageObfuscatedTypeNameOnUnknownType = ((double)nbObfuscatedTypeName)/nbUnknownTypes;
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "nbTypes=" + nbTypes +
                ", nbUnknownTypes=" + nbUnknownTypes +
                ", nbKnownTypes=" + nbKnownTypes +
                ", nbObfuscatedTypeName=" + nbObfuscatedTypeName +
                ", percentageObfuscatedTypeName=" + percentageObfuscatedTypeName +
                ", percentageObfuscatedTypeNameOnUnknownType=" + percentageObfuscatedTypeNameOnUnknownType +
                ", knownLibs=" + knownLibs +
                '}';
    }
}
