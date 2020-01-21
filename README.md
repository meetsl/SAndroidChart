# SAndroidChart
Android Chart View

2021/1/21
### 1. 饼状图
##### 实现内容：
- 动态增加数据
- 对于描述位置的冲突，牺牲空间的前提下进行避免

![image](https://img2018.cnblogs.com/blog/709594/202001/709594-20200121093213293-304244150.png)
![image](https://img2018.cnblogs.com/blog/709594/202001/709594-20200121093816711-1778321946.jpg)
![image](https://img2018.cnblogs.com/blog/709594/202001/709594-20200121093834855-266382579.jpg)
![image](https://img2018.cnblogs.com/blog/709594/202001/709594-20200121093846326-859017021.jpg)

##### 开发记录
- Android 中绘制角度的坐标：

![image](https://img2018.cnblogs.com/blog/709594/202001/709594-20200121101935463-355770924.png)

- Android中位置坐标的计算和角度的计算，注意Android 坐标系的起点位置与绘制圆圆心之间的关系。避免计算出现混乱
