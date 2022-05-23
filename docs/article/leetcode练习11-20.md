---
title: leetcode练习11-20 
date: 2018-12-16 21:01:30 
categories: [算法, LeetCode]
tags: [算法, LeetCode]
---

### 11.盛最多水的容器

给定 n 个非负整数 a1，a2，…，an，每个数代表坐标中的一个点 (i, ai) 。在坐标内画 n 条垂直线，垂直线 i 的两个端点分别为 (i, ai) 和 (i, 0)。找出其中的两条线，使得它们与 x
轴共同构成的容器可以容纳最多的水。  
**说明:** 你不能倾斜容器，且 n 的值至少为 2。  
{% chart 90% 300 %} { type: 'bar', data: { labels: ['', '', '', '', '', '', '', '', ''], datasets: [{ label: '示意图',
backgroundColor: [
'rgb(54 54 54)',
'rgb(255, 99, 132)',
'rgb(54 54 54)',
'rgb(54 54 54)',
'rgb(54 54 54)',
'rgb(54 54 54)',
'rgb(54 54 54)',
'rgb(54 54 54)',
'rgb(255, 99, 132)'
], data: [1, 8, 6, 2, 5, 4, 8, 3, 7]
}]
}, options: { responsive: true, legend: { display: false }, scales: { xAxes: [{ gridLines: { zeroLineColor: 'rgb(0,0,0)'
, lineWidth:[1,0,0,0,0,0,0,0,0,0], color: [
'rgba(0, 0, 0, 1)'
]
}, }], yAxes: [{ ticks: { beginAtZero: true }, gridLines: { lineWidth:[1,3,0,0,0,0,0,0], color: [
'rgba(0, 0, 0, 0)',
'rgb(72 118 255)'
]
}, }], }, title: { display: false, text: 'Chart.js Line Chart' }, tooltips: { enabled: false, displayColors: false } } }
{% endchart %}  
图中垂直线代表输入数组 [1,8,6,2,5,4,8,3,7]。在此情况下，容器能够容纳水（表示为蓝色部分）的最大值为 49。

#### 11.1示例

> 输入: [1,8,6,2,5,4,8,3,7]  
> 输出: 49

#### 11.2代码

```java
class Solution {
    public int maxArea(int[] height) {
        int result = 0, left = 0, right = height.length - 1;
        while (left < right) {
            result = Math.max((right - left) * Math.min(height[left], height[right]), result);
            if (height[left] < height[right]) left++;
            else right--;
        }
        return result;
    }
}
```

<!-- more -->

### 12.整数转罗马数字

罗马数字包含以下七种字符： I， V， X， L，C，D 和 M。

| 字符 | 数值 |
| :---: | :---: |
| I |   1  |
| V |   5  |
| X |  10  |
| L |  50  |
| C |  100 |
| D |  500 |
| M | 1000 |

例如， 罗马数字 2 写做 II ，即为两个并列的 1。12 写做 XII ，即为 X + II 。 27 写做 XXVII, 即为 XX + V + II 。 通常情况下，罗马数字中小的数字在大的数字的右边。但也存在特例，例如 4 不写做
IIII，而是 IV。数字 1 在数字 5 的左边，所表示的数等于大数 5 减小数 1 得到的数值 4 。同样地，数字 9 表示为 IX。这个特殊的规则只适用于以下六种情况：

* **I**可以放在**V(5)** 和 **X(10)**的左边，来表示4和9。
* **X**可以放在**L(50)** 和 **C(100)**的左边，来表示40和90。
* **C**可以放在**D(500)** 和 **M(1000)**的左边，来表示400和900。

给定一个整数，将其转为罗马数字。输入确保在 1 到 3999 的范围内。

#### 12.1示例

> 输入: 3  
> 输出: “III”

#### 12.2示例

> 输入: 4  
> 输出: “IV”

#### 12.3示例

> 输入: 9  
> 输出: “IX”

#### 12.4示例

> 输入: 58  
> 输出: “LVIII”  
> 解释: L = 50, V = 5, III = 3

#### 12.5示例

> 输入: 1994  
> 输出: “MCMXCIV”  
> 解释: M = 1000, CM = 900, XC = 90, IV = 4

```java
class Solution {
    public String intToRoman(int num) {
        int[] nums = {1, 4, 5, 9, 10, 40, 50, 90, 100, 400, 500, 900, 1000};
        String[] roman = {"I", "IV", "V", "IX", "X", "XL", "L", "XC", "C", "CD", "D", "CM", "M"};
        StringBuilder sb = new StringBuilder();
        int idx = nums.length - 1;
        while (num > 0) {
            while (num >= nums[idx]) {
                sb.append(roman[idx]);
                num -= nums[idx];
            }
            idx--;
        }
        return sb.toString();
    }
}
```

### 13.罗马数字转整数

罗马数字包含以下七种字符: I， V， X， L，C，D 和 M。

| 字符 | 数值 |
| :---: | :---: |
| I |   1  |
| V |   5  |
| X |  10  |
| L |  50  |
| C |  100 |
| D |  500 |
| M | 1000 |

例如， 罗马数字 2 写做 II ，即为两个并列的 1。12 写做 XII ，即为 X + II 。 27 写做 XXVII, 即为 XX + V + II 。 通常情况下，罗马数字中小的数字在大的数字的右边。但也存在特例，例如 4 不写做
IIII，而是 IV。数字 1 在数字 5 的左边，所表示的数等于大数 5 减小数 1 得到的数值 4 。同样地，数字 9 表示为 IX。这个特殊的规则只适用于以下六种情况：

* **I**可以放在**V(5)** 和 **X(10)** 的左边，来表示4和9。
* **X**可以放在**L(50)** 和 **C(100)** 的左边，来表示40和90。
* **C**可以放在**D(500)** 和 **M(1000)** 的左边，来表示400和900。

给定一个罗马数字，将其转换成整数。输入确保在 1 到 3999 的范围内。

#### 13.1示例

> 输入: “III”  
> 输出: 3

#### 13.2示例

> 输入: “IV”  
> 输出: 4

#### 13.3示例

> 输入: “IX”  
> 输出: 9

#### 13.4示例

> 输入: “LVIII”  
> 输出: 58  
> 解释: L = 50, V= 5, III = 3

#### 13.5示例

> 输入: “MCMXCIV”  
> 输出: 1994  
> 解释: M = 1000, CM = 900, XC = 90, IV = 4

#### 13.6代码

```java
class Solution {
    public int romanToInt(String s) {
        Map<Character, Integer> map = new HashMap<Character, Integer>() {{
            put('I', 1);
            put('V', 5);
            put('X', 10);
            put('L', 50);
            put('C', 100);
            put('D', 500);
            put('M', 1000);
        }};
        int sum = 0;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            int value = map.get(s.charAt(i));
            if (i != len - 1 && value < map.get(s.charAt(i + 1))) sum -= value;
            else sum += value;
        }
        return sum;
    }
}
```

### 14.最长公共前缀

编写一个函数来查找字符串数组中的最长公共前缀。如果不存在公共前缀，返回空字符串 “”。

#### 14.1示例

> 输入: [“flower”,”flow”,”flight”]  
> 输出: “fl”

#### 14.2示例

> 输入: [“dog”,”racecar”,”car”]  
> 输出: “”  
> 解释: 输入不存在公共前缀

**说明:** 所有输入只包含小写字母 a-z 。

#### 14.3代码

```java
class Solution {
    public String longestCommonPrefix(String[] strs) {
        if (strs.length == 0) return "";
        if (strs.length == 1) return strs[0];
        for (int len = 0; len < strs[0].length(); len++)
            for (int i = 1; i < strs.length; i++)
                if (len >= strs[i].length() || strs[i].charAt(len) != strs[0].charAt(len))
                    return strs[0].substring(0, len);
        return strs[0];
    }
}
```

### 15.三数之和

给定一个包含 n 个整数的数组 nums，判断 nums 中是否存在三个元素 a，b，c ，使得 a + b + c = 0 ？找出所有满足条件且不重复的三元组。  
**注意:** 答案中不可以包含重复的三元组。

#### 15.1示例

> 给定数组 nums = [-1, 0, 1, 2, -1, -4]，  
> 满足要求的三元组集合为：  
> [  
> [-1, 0, 1],  
> [-1, -1, 2]  
> ]

#### 15.2代码

```java
class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> list = new LinkedList<>();
        for (int left = 0; left < nums.length && nums[left] <= 0; left++) {
            if (left > 0 && nums[left] == nums[left - 1]) continue;
            int mid = left + 1, right = nums.length - 1;
            int tmpSum = -nums[left];
            while (mid < right)
                if (nums[mid] + nums[right] == tmpSum) {
                    list.add(Arrays.asList(nums[left], nums[mid], nums[right]));
                    int tmpMid = nums[mid], tmpRight = nums[right];
                    while (mid < right && nums[++mid] == tmpMid) ;
                    while (mid < right && nums[--right] == tmpRight) ;
                } else if (nums[mid] + nums[right] < tmpSum) mid++;
                else right--;
        }
        return list;
    }
}
```
