---
title: java十大排序算法分析 
date: 2018-12-17 10:21:22 
mathjax: true 
categories: [算法]
tags: [算法]
---

### 1.冒泡排序

![冒泡排序](/images/bubble_sort_anim.gif)  
它重复地走访待排序的数列，一次比较两个元素，如果它们的顺序错误，就把它们交换过来。重复的进行，直到没有需要交换为止。 冒泡排序算法的运作如下:

1. 比较相邻的元素。如果第一个比第二个大，就交换它们两个。
2. 对每一对相邻元素作同样的工作，从开始第一对到结尾的最后一对。这步做完后，最后的元素会是最大的数。
3. 针对所有的元素重复以上的步骤，除了最后一个。
4. 持续每次对越来越少的元素重复上面的步骤，直到没有任何一对数字需要比较。

#### 1.1算法复杂度

若初始文件是反序的，需要进行 趟排序。每趟排序要进行 次关键字的比较($1 \le i \le n-1$)，且每次比较都必须移动记录三次来达到交换记录位置。在这种情况下，比较和移动次数均达到最大值： $$C_{max}=\frac{n(
n-1)}{2}=O(n^2); M_{max}=\frac{3n(n-1)}{2}=O(n^2)$$  
冒泡排序的最坏时间复杂度为。综上，因此冒泡排序总的平均时间复杂度为。

#### 1.2伪代码

> BUBBLE_SORT(A)  
> for i = 1 to A.length-1  
>　for j = A.length downto i + 1  
>　if A[j] < A[j - 1]  
>　exchange A[j] with A[j-1]

#### 1.3代码

```java
class Solution {
    public void bubbleSort(int[] array) {
        for (int i = 0; i < array.length - 1; i++)
            for (int j = array.length - 1; j > i; j--) {
                if (array[j] < array[j - 1]) {
                    array[j] ^= array[j - 1];
                    array[j - 1] ^= array[j];
                    array[j] ^= array[j - 1];
                }
            }
    }
}
```

<!-- more -->

### 2.插入排序

![插入排序](/images/insertion_sort_anim.gif)  
通过构建有序序列，对于未排序数据，在已排序序列中从后向前扫描，找到相应位置并插入。插入排序在实现上，通常采用in-place排序(即只需用到O(1)的额外空间的排序)
，因而在从后向前扫描过程中，需要反复把已排序元素逐步向后挪位，为最新元素提供插入空间。 一般来说，插入排序都采用in-place在数组上实现。具体算法描述如下:

1. 从第一个元素开始，该元素可以认为已经被排序
2. 取出下一个元素，在已经排序的元素序列中从后向前扫描
3. 如果该元素(已排序)大于新元素，将该元素移到下一位置
4. 重复步骤3，直到找到已排序的元素小于或者等于新元素的位置
5. 将新元素插入到该位置后
6. 重复步骤2 ~ 5

#### 2.1算法复杂度

如果目标是把n个元素的序列升序排列，那么采用插入排序存在最好情况和最坏情况。最好情况就是，序列已经是升序排列了，在这种情况下，需要进行的比较操作需n-1次即可。最坏情况就是，序列是降序排列，那么此时需要进行的比较共有$\frac{n(
n-1)}{2}$次。插入排序的赋值操作是比较操作的次数减去n-1次，（因为n-1次循环中，每一次循环的比较都比赋值多一个，多在最后那一次比较并不带来赋值）。平均来说插入排序算法复杂度为$O(n^2)
$。因而，插入排序不适合对于数据量比较大的排序应用。但是，如果需要排序的数据量很小，例如，量级小于千；或者若已知输入元素大致上按照顺序排列，那么插入排序还是一个不错的选择。
插入排序在工业级库中也有着广泛的应用，在STL的sort算法和stdlib的qsort算法中，都将插入排序作为快速排序的补充，用于少量元素的排序（通常为8个或以下）。

#### 2.2伪代码

> INSERTION-SORT(A)  
> for j = 2 to A.length  
>　key = A[j]  
>　i = j - 1  
>　while i > 0 and A[i] > key  
>　A[i + 1] = A[i]  
>　i = i-1  
>　A[i+1] = key

#### 2.3代码

```java
class Solution {
    public void insertionSort(int[] array) {
        int key, i;
        for (int j = 1; j < array.length; j++) {
            key = array[j];
            i = j - 1;
            while (i > -1 && array[i] > key) array[i + 1] = array[i--];
            array[i + 1] = key;
        }
    }
}
```

### 3.选择排序

![选择排序](/images/selection_sort_anim.gif)  
首先在未排序序列中找到最小（大）元素，存放到排序序列的起始位置，然后，再从剩余未排序元素中继续寻找最小（大）元素，然后放到已排序序列的末尾。以此类推，直到所有元素均排序完毕。

#### 3.1复杂度分析

选择排序的交换操作介于0和(n-1)次之间。选择排序的比较操作为$\frac{n(n-1)}{2}$次。选择排序的赋值操作介于0和3(n−1)次之间。比较次数$O(n^2)$，比较次数与关键字的初始状态无关，总的比较次数$N=(n-1)+(
n-2)+…+1=\frac{n(n-1)}{2}$,交换次数O(n)最好情况是，已经有序，交换0次；最坏情况是，逆序，交换n-1次。交换次数比冒泡排序较少，由于交换所需CPU时间比比较所需的CPU时间多，n值较小时，选择排序比冒泡排序快。

#### 3.2伪代码

> SELECTION-SORT(A)  
> for i = 1 to A.length  
>　index = i  
>　for j = i + 1 to A.length  
>　if A[j] < A[index]  
>　index = j  
>　if index != i  
>　exchange A[index] with A[i]

#### 3.3代码

```java
class Solution {
    public void selectionSort(int[] array) {
        int index;
        for (int i = 0; i < array.length; i++) if (array[j] < array[index]) index = j;
        if (index != i) {
            array[index] ^= array[i];
            array[i] ^= array[index];
            array[index] ^= array[i];
        }
    }
}
```

### 4.希尔排序

![希尔排序](/images/sorting_shellsort_anim.gif)  
希尔排序通过将比较的全部元素分为几个区域来提升插入排序的性能。这样可以让一个元素可以一次性地朝最终位置前进一大步。然后算法再取越来越小的步长进行排序，算法的最后一步就是普通的插入排序，但是到了这步，需排序的数据几乎是已排好的了（此时插入排序较快）。假设有一个很小的数据在一个已按升序排好序的数组的末端。如果用复杂度为$O(
n^2)$的排序（冒泡排序或插入排序），可能会进行n次的比较和交换才能将该数据移至正确位置。而希尔排序会用较大的步长移动数据，所以小数据只需进行少数比较和交换即可到正确位置。

#### 4.1伪代码

> SHELL-SORT(A)  
> middle = A.length / 2  
> while middle > 0  
>　for i = middle to A.length  
>　temp = A[i]  
>　j = i - middle  
>　while j > -1 and A[j] > temp  
>　A[j + middle] = A[j]  
>　j = j - middle  
>　A[j + middle] = temp  
>　middle = middle / 2

#### 4.2代码

```java
class Solution {
    public void shellSort(int[] array) {
        int middle = array.length >> 1;
        int j, temp;
        while (middle > 0) {
            for (int i = middle; i < array.length; i++) {
                temp = array[i];
                j = i - middle;
                while (j > -1 && array[j] > temp) {
                    array[j + middle] = array[j];
                    j -= middle;
                }
                array[j + middle] = temp;
            }
            middle = middle >> 1;
        }
    }
}
```

### 5.堆排序

![堆排序](/images/sorting_heapsort_anim.gif)  
堆排序是指利用堆这种数据结构所设计的一种排序算法,在堆的数据结构中，堆中的最大值总是位于根节点（在优先队列中使用堆的话堆中的最小值位于根节点）。堆中定义以下几种操作：

* 最大堆调整（Max Heapify）：将堆的末端子节点作调整，使得子节点永远小于父节点
* 创建最大堆（Build Max Heap）：将堆中的所有数据重新排序
* 堆排序（HeapSort）：移除位在第一个数据的根节点，并做最大堆调整的递归运算

#### 5.1伪代码

> PARENT(i)  
> return $\left\lfloor i/2 \right\rfloor$
>
> LEFT(i)  
> return 2i
>
> RIGHT(i)  
> return 2i+1
>
> MAX-HEAPIFY(A, i)  
> l = LEFT(i)  
> r = RIGHT(i)
> if l ≤ A.heap-size and A[l] > A[i]  
> largest=l  
> else largest = i  
> if r ≤ A.heap-size and A[r] > A[largest]  
> largest = r  
> if larget ≠ i  
> exchange A[i] with A[largest]  
> MAX-HEAPIFY(A, largest)
>
> BUILD-MAX-HEAP(A)
> A.heap-size = A.length
> for i = $\left\lfloor A.length/2 \right\rfloor$ downto 1
> MAX-HEAPIFY(A, i)

#### 5.2代码

```java
class Solution {
    public void heapSort(int[] array) {
        for (int i = 0; i < array.length; i++) {
            int index = array.length - i - 1;
            maxHeap(array, index);
            if (index != 0) {
                array[0] ^= array[index];
                array[index] ^= array[0];
                array[0] ^= array[index];
            }
        }
    }

    private void maxHeap(int[] array, int lastIndex) {
        for (int i = (lastIndex - 1) / 2; i > -1; i--) {
            int k = i;
            while (2 * k <= lastIndex - 1) {
                int biggerIndex = 2 * k + 1;
                if (biggerIndex < lastIndex && array[biggerIndex] < array[biggerIndex + 1]) biggerIndex++;
                if (array[k] < array[biggerIndex]) {
                    array[k] ^= array[biggerIndex];
                    array[biggerIndex] ^= array[k];
                    array[k] ^= array[biggerIndex];
                    k = biggerIndex;
                } else break;
            }
        }
    }
}
```

### 6.快速排序

![快速排序](/images/sorting_quicksort_anim.gif)  
快速排序，又称划分交换排序，简称快排，一种排序算法，最早由东尼·霍尔提出。在平均状况下，排序n个项目要$O(\theta (n log n))$次比较。在最坏状况下则需要$O(n^2)
$次比较，但这种状况并不常见。事实上，快速排序$\theta (n log n)$通常明显比其他算法更快，因为它的内部循环可以在大部分的架构上很有效率地达成。  
快速排序采用“分而治之、各个击破”的观念，此为原地分割版本。  
快速排序使用分治法策略来把一个序列分为两个子序列。步骤为：

1. 从数列中挑出一个元素，称为“基准”，
2. 重新排序数列，所有比基准值小的元素摆放在基准前面，所有比基准值大的元素摆在基准后面（相同的数可以到任何一边）。在这个分割结束之后，该基准就处于数列的中间位置。这个称为分割操作。
3. 递归地把小于基准值元素的子数列和大于基准值元素的子数列排序。

#### 6.1伪代码

> QUICKSORT(A, p, r)  
> if p < r  
> q = PARTITION(A, p, r)  
> QUICKSORT(A, p, q - 1)  
> QUICKSORT(A, q + 1, r)
>
> PARTITION(A, p, r)  
> x = A[r]  
> i = p - 1  
> for j = p to r - 1  
> if A[j] ≤ x  
> i = i + 1  
> exchange A[i] with A[j]  
> exchange A[i + 1] with A[r]  
> return i + 1

#### 6.2 代码

```java
class Solution {
    public void quickSort(int[] array, int low, int high) {
        if (low > high || low < 0 || high > array.length) return;
        int i = low, j = high, index = array[low];
        while (i < j) {
            while (i < j && array[j] >= index) j--;
            if (i < j) array[i++] = array[j];
            while (i < j && array[i] < index) i++;
            if (i < j) array[j--] = array[i];
        }
        array[i] = index;
        quickSort(array, low, i - 1);
        quickSort(array, i + 1, high);
    }
}
```

### 7.归并排序

![归并排序](/images/merge_sort_anim.gif)  
归并操作，也叫归并算法，指的是将两个已经排序的序列合并成一个序列的操作。归并排序算法依赖归并操作。  
递归法

1. 申请空间，使其大小为两个已经排序序列之和，该空间用来存放合并后的序列
2. 设定两个指针，最初位置分别为两个已经排序序列的起始位置
3. 比较两个指针所指向的元素，选择相对小的元素放入到合并空间，并移动指针到下一位置
4. 重复步骤3直到某一指针到达序列尾
5. 将另一序列剩下的所有元素直接复制到合并序列尾

迭代法

1. 将序列每相邻两个数字进行归并操作，形成ceil(n/2)个序列，排序后每个序列包含两/一个元素
2. 若此时序列数不是1个则将上述序列再次归并，形成ceil(n/4)个序列，每个序列包含四/三个元素
3. 重复步骤2，直到所有元素排序完毕，即序列数为1

#### 7.1伪代码

> MERGE(A, p, q, r)  
> $n_1$ = q - p + 1  
> $n_2$ = r - q  
> let L [1...($n_1$+1)] and R[1...($n_2$+1)] be new arrays  
> for i = 1 to $n_1$  
> L[i] = A[p + i - 1]  
> for j = 1 to $n_1$  
> R[j] = A[q + j]  
> L[$n_1$ + 1] = $\infty$  
> R[$n_2$ + 1] = $\infty$  
> i = 1  
> j = 1  
> for k = p to r  
> if L[i] ≤ R[j]  
> A[k] = L[i]  
> i = i + 1  
> else A[k] = R[j]  
> j = j + 1
>
> MERGE-SORT(A, p, r)  
> if p < r  
> q = $\left\lfloor (p+r)/2 \right\rfloor$  
> MERGE-SORT(A, p, q)  
> MERGE-SORT(A, q + 1, r)  
> MERGE(A, p, q, r)

#### 7.2代码

```java
class Solution {
    public void mergeSort(int[] arr) {
        int[] orderedArr = new int[arr.length];
        for (int i = 2; i < arr.length * 2; i *= 2)
            for (int j = 0; j < (arr.length + i - 1) / i; j++) {
                int left = i * j;
                int mid = left + i / 2 >= arr.length ? (arr.length - 1) : (left + i / 2);
                int right = i * (j + 1) - 1 >= arr.length ? (arr.length - 1) : (i * (j + 1) - 1);
                int start = left, l = left, m = mid;
                while (l < mid && m <= right)
                    if (arr[l] < arr[m]) orderedArr[start++] = arr[l++];
                    else orderedArr[start++] = arr[m++];
                while (l < mid) orderedArr[start++] = arr[l++];
                while (m <= right) orderedArr[start++] = arr[m++];
                System.arraycopy(orderedArr, left, arr, left, right - left + 1);
            }
    }
}
```

### 8.基数排序

将所有待比较数值（正整数）统一为同样的数字长度，数字较短的数前面补零。然后，从最低位开始，依次进行一次排序。这样从最低位排序一直到最高位排序完成以后，数列就变成一个有序序列。  
基数排序的时间复杂度是 O(k ⋅ n)，其中n是排序元素个数，k是数字位数。注意这不是说这个时间复杂度一定优于 O(n ⋅ log(n))，k的大小取决于数字位的选择（比如比特位数），和待排序数据所属数据类型的全集的大小；
k决定了进行多少轮处理，而n是每轮处理的操作数目。

#### 8.1伪代码

> RADIX-SORT(A, d)  
> for i = 1 to d  
>　use a stable sort to sort array A on digit i

#### 8.2代码

```java
class Solution {
    public void radixSort(int[] array, int d) {
        int n = 1;
        for (int i = 0; i < d; i++) n *= 10;
        d = n;
        n = 1;
        int k = 0, length = array.length;
        int[][] bucket = new int[10][length];
        int[] order = new int[length];
        while (n < d) {
            for (int i : array) {
                int digit = (i / n) % 10;
                bucket[digit][order[digit]] = i;
                order[digit]++;
            }
            for (int i = 0; i < length; i++) {
                if (order[i] != 0) for (int j = 0; j < order[i]; j++) array[k++] = bucket[i][j];
                order[i] = 0;
            }
            n *= 10;
            k = 0;
        }
    }
}
```

### 9.计数排序

计数排序（Counting sort）是一种稳定的线性时间排序算法。计数排序使用一个额外的数组C ，其中第i个元素是待排序数组 A中值等于i的元素的个数。然后根据数组C
来将A中的元素排到正确的位置。通俗地理解，例如有10个年龄不同的人，统计出有8个人的年龄比A小，那A的年龄就排在第9位，用这个方法可以得到其他每个人的位置，也就排好了序。当然，年龄有重复时需要特殊处理（保证稳定性），这就是为什么最后要反向填充目标数组，以及将每个数字的统计减去1。算法的步骤如下：

1. 找出待排序的数组中最大和最小的元素
2. 统计数组中每个值为i的元素出现的次数，存入数组C的第i项
3. 对所有的计数累加（从C 中的第一个元素开始，每一项和前一项相加）
4. 反向填充目标数组：将每个元素i放在新数组的第C[i]项，每放一个元素就将C[i]减去1

#### 9.1伪代码

> COUNT-SORT(A, B, k)  
> let C[0…k] be a new array  
> for i = 0 to k  
>　C[i] = 0  
> for j = 1 to A.length  
>　C[A[j]] = C[A[j]] + 1  
> //C[i] now contains the number of elements equal to i.  
> for i = 1 to k  
>　C[i] = C[i] + C[i - 1]  
> //C[i] now contains the number of elements less than or equal to i.  
> for j = A.length downto 1  
>　B[C[A[j]]] = A[j]  
>　C[A[j]] = C[A[j]] - 1

#### 9.2代码

```java
class Solution {
    public void countSort(int[] array, int k) {
        int length = array.length, sum = 0;
        k += 1;
        int[] c = new int[k], b = new int[length];
        for (int i : array) c[i] += 1;
        for (int i = 0; i < k; i++) {
            sum += c[i];
            c[i] = sum;
        }
        for (int i = length - 1; i > -1; i--) {
            b[c[array[i]] - 1] = array[i];
            c[array[i]]--;
        }
        System.arraycopy(b, 0, array, 0, length);
    }
}
```

### 10.桶排序

桶排序（Bucket
sort）或所谓的箱排序，是一个排序算法，工作的原理是将数组分到有限数量的桶里。每个桶再个别排序（有可能再使用别的排序算法或是以递归方式继续使用桶排序进行排序）。桶排序是鸽巢排序的一种归纳结果。当要被排序的数组内的数值是均匀分配的时候，桶排序使用线性时间(
Θ(n))。但桶排序并不是比较排序，他不受到$O(n log n)$下限的影响。桶排序以下列程序进行：

1. 设置一个定量的数组当作空桶子。
2. 寻访序列，并且把项目一个一个放到对应的桶子去。
3. 对每个不是空的桶子进行排序。
4. 从不是空的桶子里把项目再放回原来的序列中。

#### 10.1伪代码

> BUCKET-SORT(A)  
> n = A.length  
> let B[0..n-1] be a new array  
> for i = 0 to n - 1  
>　make B[i] an empty list  
> for i = 1 to n  
>　insert A[i] into list B[⌊nA[i]⌋]  
> for i = 0 to n - 1  
>　sort list B[i] with insertion sort  
> concatenate the lists B[0],B[1],…,B[n-1] together in order

#### 10.2代码

```java
class Solution {
    public void bucketSort(int[] array) {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        for (int i : array) {
            max = Math.max(max, i);
            min = Math.min(min, i);
        }
        int[] temp = new int[max - min + 1];
        for (int i : array) temp[i - min]++;
        for (int i = 1; i < temp.length; i++) temp[i] = temp[i - 1] + temp[i];
        int[] result = new int[array.length];
        for (int i : array) result[--temp[i - min]] = i;
        System.arraycopy(result, 0, array, 0, array.length);
    }
}
```
