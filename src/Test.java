public class Test {

    public static void main(String[] args) {
        ImageCluster ic = new ImageCluster();
        String dir = "/Users/xiecun/Documents/Graduation/data/";
        String name = "1578538705811R";
        int k = 3;
        int m = 10;
        ic.kmeans(dir + name + ".jpg", dir + name + "_" + k + "_" + m + "_.jpg", k, m);
    }
}