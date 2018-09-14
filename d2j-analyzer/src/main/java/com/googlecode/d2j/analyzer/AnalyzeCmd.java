package com.googlecode.d2j.analyzer;

import com.googlecode.d2j.dex.writer.DexFileWriter;
import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.reader.zip.ZipUtil;
import com.googlecode.d2j.smali.Smali;
import com.googlecode.d2j.visitors.DexFileVisitor;
import com.googlecode.dex2jar.tools.BaseCmd;
import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.util.SyntheticAccessorResolver;
import org.jf.util.IndentingWriter;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@BaseCmd.Syntax(cmd = "d2j-analyzer", syntax = "[options] [--] [<smali-file>|folder]*", desc = "assembles a set of smali files into a dex file", onlineHelp = "https://sourceforge.net/p/dex2jar/wiki/Smali")
public class AnalyzeCmd extends BaseCmd {
    @Opt(opt = "x", longOpt = "allow-odex-instructions", hasArg = false, description = "[not impl] allow odex instructions to be compiled into the dex file. Only a few instructions are supported - the ones that can exist in a dead code path and not cause dalvik to reject the class")
    private boolean allowOdexInstructions;
    @Opt(opt = "a", longOpt = "api-level", description = "[not impl] The numeric api-level of the file to generate, e.g. 14 for ICS. If not specified, it defaults to 14 (ICS).", argName = "API_LEVEL")
    private int apiLevel = 14;
    @Opt(opt = "v", longOpt = "version", hasArg = false, description = "prints the version then exits")
    private boolean showVersionThenExits;
    @Opt(opt = "o", longOpt = "output", description = "the name of the dex file that will be written. The default is out.dex", argName = "FILE")
    private Path output;
    @Opt(opt = "-", hasArg = false, description = "read smali from stdin")
    private boolean readSmaliFromStdin;

    public static void main(String[] args) {
        new com.googlecode.d2j.analyzer.AnalyzeCmd().doMain(args);
    }

    @Override
    protected void doCommandLine() throws Exception {

        if (showVersionThenExits) {
            System.out.println("smali 1.4.2p (https://sourceforge.net/p/dex2jar)");
            System.out.println("Copyright (c) 2009-2013 Panxiaobo (pxb1988@gmail.com)");
            System.out.println("Apache license (http://www.apache.org/licenses/LICENSE-2.0)");
            return;
        }

        if (!readSmaliFromStdin && remainingArgs.length < 1) {
            System.err.println("ERRPR: no file to process");
            return;
        }

        if (output == null) {
            output = new File("out.dex").toPath();
        }

        Smali smali = new Smali();
        DexFileWriter fw = new DexFileWriter();

        DexFileVisitor fv = new DexFileVisitor(fw) {
            @Override
            public void visitEnd() {// intercept the call to super
            }
        };

        if (readSmaliFromStdin) {
            smali.smaliFile("<stdin>", System.in, fv);
            System.err.println("smali <stdin> -> " + output);
        }

       /* for (String s : remainingArgs) {
            Path file = new File(s).toPath();
            if (!Files.exists(file)) {
                System.err.println("skip " + file + ", it is not a dir or a file");
            } else {
                System.err.println("smali " + s + " -> " + output);
                smali.smali(file, fv);
            }
        }*/

        /*fw.visitEnd();
        byte[] data = fw.toByteArray();
        Files.write(output, data);*/

        String strDexFile=null;
        for (String s : remainingArgs) {
            Path file = new File(s).toPath();
            if (!Files.exists(file)) {
                System.err.println("skip " + file + ", it is not a dir or a file");
            } else {
                System.err.println("smali " + s + " -> " + output);
                strDexFile = s;
            }
        }
        File dexFile = new File(strDexFile);

        System.out.println(" input  File      = " + dexFile);
        System.out.println(" output Directory = " + output);

        DexBackedDexFile dex;
        try {
            dex = DexFileFactory.loadDexFile(dexFile, 14, false);

        } catch (DexBackedDexFile.NotADexFile ex) {
            ex.printStackTrace();
            return;
        }
        Map<String, DexClassNode> map = readDex(dexFile);

        Statistics statistics = new Statistics();
        for (DexBackedClassDef def : dex.getClasses()) {
            String type = def.getType();
            String toAnalyse = baksmali(def); // original
            statistics.addType(type);
        }

        statistics.finalize();
        System.out.println(statistics.toString());

    }
    private String baksmali(DexBackedClassDef def) throws IOException {
        baksmaliOptions opts = new baksmaliOptions();
        opts.outputDebugInfo = false;
        opts.syntheticAccessorResolver = new SyntheticAccessorResolver(Collections.EMPTY_LIST);
        ClassDefinition classDefinition = new ClassDefinition(opts, def);
        StringWriter bufWriter = new StringWriter();
        IndentingWriter writer = new IndentingWriter(bufWriter);
        classDefinition.writeTo((IndentingWriter) writer);
        writer.flush();
        return bufWriter.toString();
    }
    private Map<String, DexClassNode> readDex(File path) throws IOException {
        DexFileReader dexFileReader = new DexFileReader(ZipUtil.readDex(path));
        DexFileNode dexFileNode = new DexFileNode();
        dexFileReader.accept(dexFileNode);
        Map<String, DexClassNode> map = new HashMap<>();
        for (DexClassNode c : dexFileNode.clzs) {
            map.put(c.className, c);
        }
        return map;
    }
}
