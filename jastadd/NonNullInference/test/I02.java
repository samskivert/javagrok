package test;

public class I02 {
  Object f;

  void test(Object p) {
    Object v;
    f = p = v = null;
  }
  
  public static void main(String[] args) {
    System.out.println("simple inference of possibly-null property for variables");
  }
}
