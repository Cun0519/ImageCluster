public class Test {

    public static void main(String[] args){
        ImageCluster ic=new ImageCluster();
        ic.kmeans("/Users/xiecun/Documents/Graduation/data/1578538705811KR.jpg", "/Users/xiecun/Documents/Graduation/data/output.jpg",
                2,10);
    }
}