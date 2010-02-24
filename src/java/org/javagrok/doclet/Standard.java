//
// $Id$

package org.javagrok.doclet;

import java.lang.reflect.Field;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.formats.html.HtmlDoclet;
import com.sun.tools.doclets.formats.html.WriterFactoryImpl;
import com.sun.tools.doclets.internal.toolkit.ClassWriter;
import com.sun.tools.doclets.internal.toolkit.Configuration;
import com.sun.tools.doclets.internal.toolkit.MethodWriter;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;
import com.sun.tools.doclets.internal.toolkit.builders.BuilderFactory;

/**
 * The main entry point for our custom Javadoc Doclet.
 */
public class Standard
{
    public static final HtmlDoclet doclet = new HtmlDoclet();

    public static int optionLength (String option)
    {
        return HtmlDoclet.optionLength(option);
    }

    public static boolean start (RootDoc root)
    {
        return doclet.start(doclet, root);
    }

    public static boolean validOptions (String[][] options, DocErrorReporter reporter)
    {
        return HtmlDoclet.validOptions(options, reporter);
    }

    public static LanguageVersion languageVersion ()
    {
        return HtmlDoclet.languageVersion();
    }

    protected static void injectWriterFactory ()
    {
        ConfigurationImpl conf = ConfigurationImpl.getInstance();
        BuilderFactory bf = new BuilderFactory(conf);
        setField(BuilderFactory.class, "writerFactory", bf, new WriterFactoryImpl(conf) {
            public ClassWriter getClassWriter (
                ClassDoc classDoc, ClassDoc prevClass,
                ClassDoc nextClass, ClassTree classTree) throws Exception {
                return new GrokClassWriter(classDoc, prevClass, nextClass, classTree);
            }
            public MethodWriter getMethodWriter (ClassWriter classWriter) throws Exception {
                return new GrokMethodWriter(classWriter);
            }
        });
        setField(Configuration.class, "builderFactory", conf, bf);
    }

    protected static void setField (Class<?> clazz, String name, Object receiver, Object value)
    {
        try {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getName().equals(name)) {
                    f.setAccessible(true);
                    f.set(receiver, value);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        System.err.println("Failed to inject " + clazz.getName() + "." + name + ".");
    }

    // someone find the people who wrote javadoc and poke them in the eye
    static { injectWriterFactory(); }
}
