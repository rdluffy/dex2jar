import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFieldNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.reader.zip.ZipUtil;
import com.googlecode.d2j.smali.Baksmali;
import com.googlecode.d2j.smali.Smali;
import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.baksmali.Adaptors.MethodDefinition;
import org.jf.baksmali.Adaptors.MethodItem;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.util.SyntheticAccessorResolver;
import org.jf.util.IndentingWriter;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.dex2jar.tools.BaseCmd.getBaseName;

public class StefJoue {
    static void studyClassDef(ClassDefinition classDefinition){

        System.out.println(classDefinition.classDef.getType());
        ClassDef classDef = classDefinition.classDef;
        Iterable<? extends Method> directMethods;
        if (classDef instanceof DexBackedClassDef) {
            directMethods = ((DexBackedClassDef)classDef).getDirectMethods(false);
        } else {
            directMethods = classDef.getDirectMethods();
        }

        for (Method method: directMethods) {
            MethodImplementation methodImpl = method.getImplementation();
            if (methodImpl == null) {
//                MethodDefinition.writeEmptyMethodTo(methodWriter, method, options);
                System.out.println("ici");
            } else {
                MethodDefinition methodDefinition = new MethodDefinition(classDefinition, method, methodImpl);
//                methodDefinition.writeTo(methodWriter);
                System.out.println("la");
////                List<MethodItem> methodItems = methodDefinition.getMethodItems();
//                for (MethodItem methodItem: methodItems) {
//
//                }

            }
        }
    }
    private static String baksmali(DexBackedClassDef def) throws IOException {
        baksmaliOptions opts = new baksmaliOptions();
        opts.outputDebugInfo = false;
        opts.syntheticAccessorResolver = new SyntheticAccessorResolver(Collections.EMPTY_LIST);
        ClassDefinition classDefinition = new ClassDefinition(opts, def);
        StringWriter bufWriter = new StringWriter();
        IndentingWriter writer = new IndentingWriter(bufWriter);
        classDefinition.writeTo((IndentingWriter) writer);
        studyClassDef(classDefinition);
        writer.flush();
        return bufWriter.toString();
    }
    static Map<String, DexClassNode> readDex(File path) throws IOException {
        DexFileReader dexFileReader = new DexFileReader(ZipUtil.readDex(path));
        DexFileNode dexFileNode = new DexFileNode();
        dexFileReader.accept(dexFileNode);
        Map<String, DexClassNode> map = new HashMap<>();
        for (DexClassNode c : dexFileNode.clzs) {
            map.put(c.className, c);
        }
        return map;
    }

    public static void main2(String[] args) throws IOException {
        File dex = new File("C:\\tmp\\watson\\analysis\\apk\\Deezloader.apk");
        Baksmali b = Baksmali.from(dex);
        Path output=null;
        if (output == null) {
            output = new File("C:\\tmp\\watson\\analysis\\apk\\out\\"+getBaseName(dex.getName()) + "-out").toPath();
        }
        System.err.println("baksmali " + dex + " -> " + output);
        b.to(output);
    }
    public static void main(String[] args) throws IOException {
        File dexFile = new File("C:\\tmp\\watson\\analysis\\apk\\com.lbp.peps_2018-08-01.apk");
        DexBackedDexFile dex;
        try {
            dex = DexFileFactory.loadDexFile(dexFile, 14, false);

        } catch (DexBackedDexFile.NotADexFile ex) {
            ex.printStackTrace();
            return;
        }
        Map<String, DexClassNode> map = readDex(dexFile);

        int which  = 0;
        for (DexBackedClassDef def : dex.getClasses()) {
            which++;
            String type = def.getType();
            if(which != 40){
                continue;
            }
            System.out.println(">>"+type);
            DexClassNode dexClassNode = map.get(type);
            Assert.assertNotNull(dexClassNode);
            String smali = baksmali(def); // original
            if(smali.contains("const-string")){
                System.out.println(which);
                System.out.println(type);
                System.out.println(smali);
                break;
            }
//            break;
          //  Smali.smaliFile2Node("fake.smali", smali);

            /*{
                byte[] data = toDex(dexClassNode);
                DexBackedClassDef def2 = new DexBackedDexFile(new Opcodes(14, false), data).getClasses().iterator().next();
                String baksmali3 = baksmali(def2); // original
                Assert.assertEquals(smali, baksmali3);
            }

            String psmali = pbaksmali(dexClassNode);
            DexClassNode dexClassNode2 = Smali.smaliFile2Node("fake.smali", psmali);
            Assert.assertEquals("cmp smalip", psmali, pbaksmali(dexClassNode2));

            {
                byte[] data = toDex(dexClassNode2);
                DexBackedClassDef def2 = new DexBackedDexFile(new Opcodes(14, false), data).getClasses().iterator().next();
                String baksmali3 = baksmali(def2); // original
                Assert.assertEquals(smali, baksmali3);
            }*/
        }
    }
}
