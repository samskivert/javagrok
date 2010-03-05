//
// Function -

package uclisp;

public class Function
{
    //
    // Function public member functions

    public Object evaluate (Interpreter interp, List sexp)
        throws RunTimeException
    {
        throw new UnsupportedOperationException();
    }

    public int numArguments ()
    {
        return -1;
    }

    public String name ()
    {
        return getClass().getName().substring(1);
    }

    public void verifyArguments (List sexp)
        throws RunTimeException
    {
        if (numArguments() == -1) return;
        if (numArguments() != sexp.size())
            throw new RunTimeException("Illegal call to " +
                                       numArguments() + " arg function [" +
                                       name() + "] with " +
                                       sexp.size() + " args", sexp);
    }
}
