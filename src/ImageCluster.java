import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class ImageCluster {
    //主要功能就是读取一副图像，再对图像进行分割
    //需要分类的簇数
    private int k;
    //迭代次数
    private int m;
    //数据集合
    private dataItem[][] source;
    //中心集合
    private dataItem[] center;
    //统计每个簇的各项数据的总和，用于计算新的点数
    private dataItem[] centerSum;

    private double[] rgbNum;

    //读取指定目录的图片数据，并且写入数组，这个数据要继续处理
    private int[][] getImageData(String path) {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int width = bi.getWidth();
        int height = bi.getHeight();
        int[][] data = new int[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                data[i][j] = bi.getRGB(i, j);
        return data;
    }

    //用来处理获取的像素数据，提取我们需要的写入dataItem数组
    private dataItem[][] InitData(int[][] data) {
        dataItem[][] dataitems = new dataItem[data.length][data[0].length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                dataItem di = new dataItem();
                Color c = new Color(data[i][j]);
                di.r = (double) c.getRed();
                di.g = (double) c.getGreen();
                di.b = (double) c.getBlue();
                di.group = 1;
                dataitems[i][j] = di;
            }
        }
        return dataitems;
    }

    //生成随机的初始中心
    private void initCenters(int k) {
        center = new dataItem[k];
        //用来统计每个聚类里面的RGB分别之和，方便计算均值
        centerSum = new dataItem[k];
        int width, height;
        for (int i = 0; i < k; i++) {
            dataItem cent = new dataItem();
            dataItem cent2 = new dataItem();

            width = (int) (Math.random() * source.length);
            height = (int) (Math.random() * source[0].length);
            cent.group = i;
            cent.r = (double) source[width][height].r;
            cent.g = (double) source[width][height].g;
            cent.b = (double) source[width][height].b;
            center[i] = cent;


            cent2.r = cent.r;
            cent2.g = cent.g;
            cent2.b = cent.b;
            cent2.group = 0;
            centerSum[i] = cent2;

            width = 0;
            height = 0;
        }
    }

    //计算两个像素之间的欧式距离，用RGB作为三维坐标
    private double distance(dataItem first, dataItem second) {
        double distance = 0;
        distance = Math.sqrt(Math.pow((first.r - second.r), 2) + Math.pow((first.g - second.g), 2) +
                Math.pow((first.b - second.b), 2));
        return distance;
    }

    //返回一个数组中最小的坐标
    private int minDistance(double[] distance) {
        double minDistance = distance[0];
        int minLocation = 0;
        for (int i = 0; i < distance.length; i++) {
            if (distance[i] < minDistance) {
                minDistance = distance[i];
                minLocation = i;
            }
        }
        return minLocation;
    }

    //每个点进行分类
    private void clusterSet() {
        int group;
        double distance[] = new double[k];
        for (int i = 0; i < source.length; i++) {
            for (int j = 0; j < source[0].length; j++) {
                //求出该点与各个中心之间的距离
                for (int q = 0; q < center.length; q++) {
                    distance[q] = distance(center[q], source[i][j]);
                }
                //寻找距离该点最近的中心
                group = minDistance(distance);
                //把该点进行分类
                source[i][j].group = group;
                //分类完求出该类的RGB和
                centerSum[group].r += source[i][j].r;
                centerSum[group].g += source[i][j].g;
                centerSum[group].b += source[i][j].b;
                //这个就是用来统计聚类里有几个点
                centerSum[group].group += 1;
            }
        }
    }

    //设置新的中心
    public void setNewCenter() {
        for (int i = 0; i < centerSum.length; i++) {
            //取平均值为新的中心
            center[i].r = (int) (centerSum[i].r / centerSum[i].group);
            center[i].g = (int) (centerSum[i].g / centerSum[i].group);
            center[i].b = (int) (centerSum[i].b / centerSum[i].group);
            //重置之前的求和结果
            centerSum[i].r = center[i].r;
            centerSum[i].g = center[i].g;
            centerSum[i].b = center[i].b;
            centerSum[i].group = 0;
        }
    }

    //输出聚类好的数据
    private void ImagedataOut(String path) {
        Color white = new Color(255, 255, 255);
        Color black = new Color(0, 0, 0);
        BufferedImage nbi = new BufferedImage(source.length, source[0].length, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < centerSum.length; i++) {
            rgbNum[i] = centerSum[i].r + centerSum[i].g + centerSum[i].b;
        }
        //0为第一个数组下标
        double num = rgbNum[0];
        int flag = 0;
        //开始循环一维数组
        for (int i = 0; i < rgbNum.length; i++) {
            if (rgbNum[i] < num) {
                num = rgbNum[i];
                flag = i;
            }
        }
        for (int i = 0; i < source.length; i++) {
            for (int j = 0; j < source[0].length; j++) {
                if (source[i][j].group == flag) {
                    nbi.setRGB(i, j, black.getRGB());
                } else {
                    nbi.setRGB(i, j, white.getRGB());
                }
            }
        }

        try {
            ImageIO.write(nbi, "jpg", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //进行kmeans计算的核心函数
    public void kmeans(String originPath, String outputPath, int k, int m) {

        this.k = k;
        this.m = m;

        rgbNum = new double[k];
        source = InitData(getImageData(originPath));

        //初始化聚类中心
        initCenters(k);

        //进行m次聚类
        for (int level = 0; level < m; level++) {
            clusterSet();
            setNewCenter();
        }
        clusterSet();

        ImagedataOut(outputPath);
    }

}

class dataItem {
    public double r;
    public double g;
    public double b;
    public int group;
}