//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import static com.samskivert.Log.log;

/**
 * Convenience methods for streams.
 */
public class StreamUtil
{
    /**
     * Convenient close for a stream. Use in a finally clause and love life.
     */
    public static void close (InputStream in)
    {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ioe) {
                log.warning("Error closing input stream", "stream", in, "cause", ioe);
            }
        }
    }

    /**
     * Convenient close for a stream. Use in a finally clause and love life.
     */
    public static void close (OutputStream out)
    {
        if (out != null) {
            try {
                out.close();
            } catch (IOException ioe) {
                log.warning("Error closing output stream", "stream", out, "cause", ioe);
            }
        }
    }

    /**
     * Convenient close for a Reader. Use in a finally clause and love life.
     */
    public static void close (Reader in)
    {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ioe) {
                log.warning("Error closing reader", "reader", in, "cause", ioe);
            }
        }
    }

    /**
     * Convenient close for a Writer. Use in a finally clause and love life.
     */
    public static void close (Writer out)
    {
        if (out != null) {
            try {
                out.close();
            } catch (IOException ioe) {
                log.warning("Error closing writer", "writer", out, "cause", ioe);
            }
        }
    }

    /**
     * Copies the contents of the supplied input stream to the supplied output stream.
     */
    public static <T extends OutputStream> T copy (InputStream in, T out)
        throws IOException
    {
        byte[] buffer = new byte[4096];
        for (int read = 0; (read = in.read(buffer)) > 0; ) {
            out.write(buffer, 0, read);
        }
        return out;
    }

    /**
     * Reads the contents of the supplied stream into a byte array.
     */
    public static byte[] toByteArray (InputStream stream)
        throws IOException
    {
        return copy(stream, new ByteArrayOutputStream()).toByteArray();
    }

    /**
     * Reads the contents of the supplied stream into a string using the platform default charset.
     */
    public static String toString (InputStream stream)
        throws IOException
    {
        return copy(stream, new ByteArrayOutputStream()).toString();
    }

    /**
     * Reads the contents of the supplied stream into a string using the supplied {@link Charset}.
     */
    public static String toString (InputStream stream, String charset)
        throws IOException
    {
        return copy(stream, new ByteArrayOutputStream()).toString(charset);
    }

    /**
     * Reads the contents of the supplied reader into a string.
     */
    public static String toString (Reader reader)
        throws IOException
    {
        char[] inbuf = new char[4096];
        StringBuffer outbuf = new StringBuffer();
        for (int read = 0; (read = reader.read(inbuf)) > 0; ) {
            outbuf.append(inbuf, 0, read);
        }
        return outbuf.toString();
    }
}
