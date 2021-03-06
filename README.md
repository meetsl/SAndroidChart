# SAndroidChart
Android Chart View

### 1. 饼状图
##### 实现内容：
- 动态增加数据
- 对于描述位置的冲突，牺牲空间的前提下进行避免
- 增加控制变量，中间可显示内容，以及描述最大长度，超过折行显示。半径控制

![image](https://img2018.cnblogs.com/blog/709594/202001/709594-20200121093816711-1778321946.jpg)
![image](https://img2018.cnblogs.com/blog/709594/202001/709594-20200121093834855-266382579.jpg)

##### 开发记录
- Android 中绘制角度的坐标：

![image](https://img2018.cnblogs.com/blog/709594/202001/709594-20200121101935463-355770924.png)

- Android中位置坐标的计算和角度的计算，注意Android 坐标系的起点位置与绘制圆圆心之间的关系。避免计算出现混乱

### 2. 柱状图
- 可左右滑动查看更多数据
- 响应点击查看数据详情
- 折线的平滑处理
- Y轴正轴和辅轴均支持数字和百分比(%)显示
- X轴支持年、月、日，以及开头年份始终显示
- 单一显示控制，可以只显示柱状图或者折线图
- 柱状图柱顶表示值显示

![image](https://img2018.cnblogs.com/common/709594/202002/709594-20200220111212282-1509670776.jpg)
![image](https://img2018.cnblogs.com/common/709594/202002/709594-20200220111227791-500890208.jpg)

##### 开发记录
- 在滑动过程中折线边界点显示的计算，左侧：( 前一个未显示点,第一个显示点 )与Y轴的交点；右侧：( 最后一个点,即将进入点 )与Y辅轴的交点

### 3. 环形进度条
- 显示 totalAngle 角度可以该更改
- 进度可动态显示

![image](https://img2018.cnblogs.com/common/709594/202002/709594-20200220120716156-448096233.jpg)
![image](https://img2018.cnblogs.com/common/709594/202002/709594-20200220120726644-1259990663.jpg)
