---
title: leetcode练习1-10 
date: 2018-12-16 10:38:00 
mathjax: true 
categories: [算法, LeetCode]
tags: [算法, LeetCode]
---

### 1.两数之和

给定一个整数数组 nums 和一个目标值 target，请你在该数组中找出和为目标值的那 两个 整数，并返回他们的数组下标。你可以假设每种输入只会对应一个答案。但是，你不能重复利用这个数组中同样的元素。

#### 1.1示例

> 给定 nums = [2, 7, 11, 15], target = 9  
> 因为 nums[0] + nums[1] = 2 + 7 = 9  
> 所以返回 [0, 1]

#### 1.2代码

```java
class Solution {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        int i = 0;
        while (i < nums.length) {
            int complement = target - nums[i];
            if (map.containsKey(complement)) return new int[]{map.get(complement), i};
            map.put(nums[i], i++);
        }
        throw new IllegalArgumentException("No two sum solution");
    }
}
```

<!-- more -->

### 2.两数相加

给出两个 非空 的链表用来表示两个非负的整数。其中，它们各自的位数是按照 逆序 的方式存储的，并且它们的每个节点只能存储 一位 数字。如果，我们将这两个数相加起来，则会返回一个新的链表来表示它们的和。您可以假设除了数字 0
之外，这两个数都不会以 0 开头。

#### 2.1示例

> 输入: (2 -> 4 -> 3) + (5 -> 6 -> 4)  
> 输出: 7 -> 0 -> 8  
> 原因: 342 + 465 = 807

#### 2.2代码

```java
class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode dummyHead = new ListNode(0);
        ListNode p = l1, q = l2, curr = dummyHead;
        int carry = 0;
        while (p != null || q != null) {
            int x = (p != null) ? p.val : 0;
            int y = (q != null) ? q.val : 0;
            int sum = carry + x + y;
            carry = sum / 10;
            curr.next = new ListNode(sum % 10);
            curr = curr.next;
            if (p != null) p = p.next;
            if (q != null) q = q.next;
        }
        if (carry > 0) curr.next = new ListNode(carry);
        return dummyHead.next;
    }
}
```

### 3.无重复字符的最长子串

给定一个字符串，请你找出其中不含有重复字符的 最长子串 的长度。

#### 3.1示例

> 输入: “abcabcbb”  
> 输出: 3  
> 解释: 因为无重复字符的最长子串是 “abc”，所以其长度为 3。

#### 3.2示例

> 输入: “bbbbb”  
> 输出: 1  
> 解释: 因为无重复字符的最长子串是 “b”，所以其长度为 1。

#### 3.3示例

> 输入: “pwwkew”  
> 输出: 3  
> 解释: 因为无重复字符的最长子串是 “wke”，所以其长度为 3。请注意，你的答案必须是 子串 的长度，”pwke” 是一个子序列，不是子串。

#### 3.4代码

```java
class Solution {
    public int lengthOfLongestSubstring(String s) {
        int[] m = new int[256];
        Arrays.fill(m, -1);
        int res = 0, left = -1;
        for (int i = 0; i < s.length(); ++i) {
            left = Math.max(left, m[s.charAt(i)]);
            m[s.charAt(i)] = i;
            res = Math.max(res, i - left);
        }
        return res;
    }
}
```

### 4.寻找两个有序数组的中位数

给定两个大小为 m 和 n 的有序数组 nums1 和 nums2。请你找出这两个有序数组的中位数，并且要求算法的时间复杂度为 O(log(m + n))。你可以假设 nums1 和 nums2 不会同时为空。

#### 4.1示例

> 输入: nums1 = [1, 3]  
> 输入: nums2 = [2]  
> 输出: 2

#### 4.2示例

> 输入: nums1 = [1, 2]  
> 输入: nums2 = [3, 4]  
> 输出: 2.5  
> 解释: 则中位数是 (2 + 3)/2 = 2.5

#### 4.3代码

```java
class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int len = nums2.length;
        int[] nums = new int[nums1.length + len];
        int index = 0, j = 0;
        for (int num : nums1) {
            while (j < len && num > nums2[j]) nums[index++] = nums2[j++];
            nums[index++] = num;
        }
        while (j < len) nums[index++] = nums2[j++];
        int middle = nums.length >> 1;
        return (nums.length & 1) == 1 ? nums[middle] : (nums[middle - 1] + nums[middle]) / 2.0;
    }
}
```

### 5.最长回文子串

给定一个字符串 s，找到 s 中最长的回文子串。你可以假设 s 的最大长度为 1000。

#### 5.1示例

> 输入: “babad”  
> 输出: “bab”  
> 注意: “aba” 也是一个有效答案。

#### 5.2示例

> 输入: “cbbd”  
> 输出: “bb”

#### 5.3代码

```java
class Solution {
    public String longestPalindrome(String s) {
        if (s.isEmpty() || s.length() == 1) return s;
        char[] arr = s.toCharArray();
        int[] ret = new int[2];
        for (int i = 0; i < arr.length; i++) i = loop(arr, i, ret);
        return s.substring(ret[0], ret[1]);
    }

    private int loop(char[] arr, int low, int[] ret) {
        int high = low;
        while (high < arr.length - 1 && arr[high + 1] == arr[low]) high++;
        int res = high;
        while (low >= 0 && high < arr.length && arr[low] == arr[high]) {
            low--;
            high++;
        }
        if (high - low - 1 > ret[1] - ret[0]) {
            ret[1] = high;
            ret[0] = low + 1;
        }
        return res;
    }
}
```

### 6.Z字形变换

将一个给定字符串根据给定的行数，以从上往下、从左到右进行 Z 字形排列。比如输入字符串为 “LEETCODEISHIRING” 行数为 3 时，排列如下：
> L C I R  
> E T O E S I I G  
> E D H N

之后，你的输出需要从左往右逐行读取，产生出一个新的字符串，比如：”LCIRETOESIIGEDHN”。请你实现这个将字符串进行指定行数变换的函数：
> string convert(string s, int numRows);

#### 6.1示例

> 输入: s = “LEETCODEISHIRING”, numRows = 3  
> 输出: “LCIRETOESIIGEDHN”

#### 6.2示例

> 输入: s = “LEETCODEISHIRING”, numRows = 4  
> 输出: “LDREOEIIECIHNTSG”  
> 解释:
> L&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;D&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R  
> E&nbsp;&nbsp;&nbsp;&nbsp;O&nbsp;E&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;I&nbsp;&nbsp;I  
> E&nbsp;C&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;I&nbsp;H&nbsp;&nbsp;&nbsp;&nbsp;N  
> T&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;S&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;G

#### 6.3代码

```java
class Solution {
    public String convert(String s, int numRows) {
        if (numRows < 2) return s;
        StringBuilder result = new StringBuilder();
        int len = s.length();
        int nodeLen = (numRows << 1) - 2;
        for (int i = 0; i < numRows; i++)
            for (int j = 0; j + i < len; j += nodeLen) {
                result.append(s.charAt(j + i));
                if (i != 0 && i != numRows - 1 && j - i + nodeLen < len) result.append(s.charAt(j - i + nodeLen));
            }
        return result.toString();
    }
}
```

### 7.整数反转

给出一个 32 位的有符号整数，你需要将这个整数中每位上的数字进行反转。

#### 7.1示例

> 输入: 123  
> 输出: 321

#### 7.2示例

> 输入: -123  
> 输出: -321

#### 7.3示例

> 输入: 120  
> 输出: 21

**注意:** 假设我们的环境只能存储得下 32 位的有符号整数，则其数值范围为 [$−2^{31}, 2^{31} − 1$]。请根据这个假设，如果反转后整数溢出那么就返回 0。

#### 7.4代码

```java
class Solution {
    public int reverse(int x) {
        long result = 0L;
        while (x != 0) {
            int r = x % 10;
            x = x / 10;
            result = result * 10 + r;
        }
        if (result >= Integer.MAX_VALUE || result <= Integer.MIN_VALUE) result = 0;
        return (int) result;
    }
}
```

### 8.字符串转换整数

请你来实现一个 atoi
函数，使其能将字符串转换成整数。首先，该函数会根据需要丢弃无用的开头空格字符，直到寻找到第一个非空格的字符为止。当我们寻找到的第一个非空字符为正或者负号时，则将该符号与之后面尽可能多的连续数字组合起来，作为该整数的正负号；假如第一个非空字符是数字，则直接将其与之后连续的数字字符组合起来，形成整数。该字符串除了有效的整数部分之后也可能会存在多余的字符，这些字符可以被忽略，它们对于函数不应该造成影响。注意：假如该字符串中的第一个非空格字符不是一个有效整数字符、字符串为空或字符串仅包含空白字符时，则你的函数不需要进行转换。在任何情况下，若函数不能进行有效的转换时，请返回
0。  
**说明:**  
假设我们的环境只能存储 32 位大小的有符号整数，那么其数值范围为 [$−2^{31}, 2^{31} − 1$]。如果数值超过这个范围，qing返回 INT_MAX ($2^{31} − 1$) 或 INT_MIN ($−2^{31}$)
。

#### 8.1示例

> 输入: “42”  
> 输出: 42

#### 8.2示例

> 输入: “ -42”  
> 输出: -42  
> 解释: 第一个非空白字符为 ‘-‘, 它是一个负号。我们尽可能将负号与后面所有连续出现的数字组合起来，最后得到 -42 。

#### 8.3示例

> 输入: “4193 with words”  
> 输出: 4193  
> 解释: 转换截止于数字 ‘3’ ，因为它的下一个字符不为数字。

#### 8.4示例

> 输入: “words and 987”  
> 输出: 0  
> 解释: 第一个非空字符是 ‘w’, 但它不是数字或正、负号。因此无法执行有效的转换。

#### 8.5示例

> 输入: “-91283472332”  
> 输出: -2147483648  
> 解释: 数字 “-91283472332” 超过 32 位有符号整数范围。因此返回 INT_MIN (−231) 。

#### 8.6代码

```java
class Solution {
    public int myAtoi(String str) {
        str = str.trim();
        int result = 0;
        boolean isPos = true;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i == 0 && (c == '-' || c == '+')) isPos = c == '+';
            else if (c >= '0' && c <= '9') {
                if (result > (Integer.MAX_VALUE - (c - '0')) / 10) return isPos ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                result = result * 10 + c - '0';
            } else return isPos ? result : -result;
        }
        return isPos ? result : -result;
    }
}
```

### 9.回文数

判断一个整数是否是回文数。回文数是指正序（从左向右）和倒序（从右向左）读都是一样的整数。

#### 9.1示例

> 输入: 121  
> 输出: true

#### 9.2示例

> 输入: -121  
> 输出: false  
> 解释: 从左向右读, 为 -121 。 从右向左读, 为 121- 。因此它不是一个回文数。

#### 9.3示例

> 输入: 10  
> 输出: false  
> 解释: 从右向左读, 为 01 。因此它不是一个回文数。

#### 9.4代码

```java
class Solution {
    public boolean isPalindrome(int x) {
        int origin = x;
        int result = 0;
        int temp;
        while (x > 0) {
            temp = x % 10;
            x /= 10;
            result = result * 10 + temp;
        }
        return result == origin;
    }
}
```

### 10.正则表达式匹配

给定一个字符串 (s) 和一个字符模式 (p)。实现支持 ‘.’ 和 ‘*‘ 的正则表达式匹配
> ‘.’ 匹配任意单个字符。  
> ‘*‘ 匹配零个或多个前面的元素。

匹配应该覆盖整个字符串 (s) ，而不是部分字符串。  
**说明:**

* s 可能为空，且只包含从 a-z 的小写字母。
* p 可能为空，且只包含从 a-z 的小写字母，以及字符 . 和 * 。

#### 10.1示例

> 输入: s = “aa”, p = “a”  
> 输出: false  
> 解释: “a” 无法匹配 “aa” 整个字符串。

#### 10.2示例

> 输入: s = “aa”, p = “a*“  
> 输出: true  
> 解释: ‘*‘ 代表可匹配零个或多个前面的元素, 即可以匹配 ‘a’ 。因此, 重复 ‘a’ 一次, 字符串可变为 “aa”。

#### 10.3示例

> 输入: s = “ab”, p = “.*“  
> 输出: true  
> 解释: ‘.*‘ 表示可匹配零个或多个(‘*‘)任意字符(‘.’)。

#### 10.4示例

> 输入: s = “aab”, p = “c*a*b”  
> 输出: true  
> 解释: ‘c’ 可以不被重复, ‘a’ 可以被重复一次。因此可以匹配字符串 “aab”。

#### 10.5示例

> 输入: s = “mississippi”, p = “mis*is*p*.”  
> 输出: false

#### 10.6代码

```java
class Solution {
    public boolean isMatch(String s, String p) {
        boolean[] match = new boolean[s.length() + 1];
        match[s.length()] = true;
        for (int i = p.length() - 1; i >= 0; i--)
            if (p.charAt(i) == '*') {
                for (int j = s.length() - 1; j >= 0; j--)
                    match[j] = match[j] || (match[j + 1] && (p.charAt(i - 1) == '.' || p.charAt(i - 1) == s.charAt(j)));
                i--;
            } else {
                for (int j = 0; j < s.length(); j++)
                    match[j] = match[j + 1] && (p.charAt(i) == '.' || p.charAt(i) == s.charAt(j));
                match[s.length()] = false;
            }
        return match[0];
    }
}
```
