---
title: Android常用加密解密实现方式 
date: 2018-12-25 21:18:22 
mathjax: true 
categories: [Android]
tags: [加密]
---

### 1.MD5，SHA1加密校验

MD5,SHA1等加密算法我们通常不用来做加密，因为解密成本非常大，我们一般用MD5，SHA1等用来做文件校验，唯一性校验等功能。常见的场景如防止别人恶意篡改我们的APP软件，这个时候我们可以对文件做MD5或SHA1算法校验，生成一个唯一值。用户通过这两个值可以判断这个APP是否为官方渠道。算法如下:

```java
public class Demo {
    /**
     * MD5加密.
     *
     * @param stream 待加密数据
     * @param type   加密类型，如MD5,SHA-1, SHA-224, SHA-256, SHA-384, SHA-512
     * @return 加密后的数据
     */
    public static byte[] md5(InputStream stream, String type) {
        try {
            MessageDigest md5 = MessageDigest.getInstance(type);
            DigestInputStream dis = new DigestInputStream(stream, md5);
            byte[] buffer = new byte[4096];
            while (dis.read(buffer) > 0) ;
            return dis.getMessageDigest().digest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
```

如果只是对普通文本信息做加密校验的话，就不需要`DigestInputStream`，直接使用`md5.digest(data);`就可以了。

<!-- more -->

### 2.AES对称加密

对称加密算法有DES, 3DES,
AES等算法，在对称加密中，加密和解密的密钥相同，一般用于安全性不是很高的信息加密，其中DES加密算法出现的较早，随着互联网技术的发展，破解DES算法的成本越来越低，已经达不到最初的加密效果，于是又提出了AES对称加密算法，AES加密方式为字节代替，行移位，列混淆和轮密钥加。代码如下:

```java
public class Demo {
    /**
     * AES加密解密，填充方式为CBC，补码为PKCS5Padding,注意密钥和偏移量必须是16个字节.
     *
     * @param encrypt true为加密，false为解密
     * @param data    待加密解密数据
     * @param key     加密解密密钥，必须16个字节
     * @param iv      偏移量，必须16个字节
     * @return 返回加密或解密后的数据
     */
    public static byte[] aes(boolean encrypt, byte[] data, byte[] key, byte[] iv) {
        if (data != null || key != null && iv != null && key.length == 16 && iv.length == 16) {
            SecretKeySpec spec = new SecretKeySpec(key, "AES");
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                int mode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
                cipher.init(mode, spec, new IvParameterSpec(iv));
                return cipher.doFinal(data);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
```

其中的填充方式有ECB, CBC, CTR, CFB,
OFB。除ECB方式外，其它方式都需要一个16字节的偏移量，补码方式为PKCS5Padding，PKCS7Padding，ZEROPadding，ISO10126和ANSIX923。其中PKCS5Padding和PKCS7Padding的填充方式效果一样PKCS5Padding常用于Android加密，PKCS7Padding常用于iOS加密。

### 3.RSA非对称加密

在有些场景下对对数据的安全性要求比较就，这个时候我们就不能用对称加密将密钥暴露出来。我们可以通过RSA非对称加密来完成这样的功能，RSA加密分公钥加密解密和私钥加密解密，通常我们将公钥公布在外对信息加密，将私钥放在服务器进行解密，这样别人通过抓包的方式获取到数据也无法解密，具体代码如下:

```java
public class Demo {
    /**
     * 生成RSA密钥对，密钥长度为512/1024/2048
     *
     * @param keyLength 密钥长度
     * @return 返回密钥对
     */
    public static KeyPair generateRSAKeyPair(int keyLength) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(keyLength);
            return kpg.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * RSA公钥加密解密.
     *
     * @param encrypt true为加密，false为解密
     * @param data    待加密数据
     * @param key     公钥
     * @return 加密或解密后的数据
     */
    public static byte[] rsaPublicKey(boolean encrypt, byte[] data, byte[] key) {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = factory.generatePublic(spec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            int mode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(mode, publicKey);
            return cipher.doFinal(data);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * RSA私钥加密解密.
     *
     * @param encrypt true为加密，false为解密
     * @param data    待加密解密数据
     * @param key     私钥
     * @return 返回加密或解密后的数据
     */
    public static byte[] rsaPrivateKey(boolean encrypt, byte[] data, byte[] key) {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = factory.generatePrivate(spec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            int mode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(mode, privateKey);
            return cipher.doFinal(data);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
}
```

### 4.Base64加密

Base64加密常用于数据传输打印等，如前面用AES,RSA加密后的字节，很多情况下我们打印出来是一串乱码或明显不是我们想要的，还有一些情况下我们直接传输字节数组不是很方便，若我们将字节数组转成字符串就很方便传输了。Base64中有64个字符，在0个数字加上大小写字母52个再加两个字符+和/正好64个字符。64个字符我们只需要6位就能表示，正常一个字节是8位，而Base64中一个字节是6位，因此普通3个字节转成Base64后，会变成4个字节，长度会增加1/3。在转成Base64过程中，会出现位置不够24位，这个时候在末尾补0用=代替。Android
SDK已实现Base64算法，我们可以直接使用或简单的封装一下就可以了，代码如下:

```java
public class Demo {
    public static byte[] base64(boolean encrypt, byte[] data, int flags) {
        if (encrypt) {
            return Base64.encode(data, flags);
        } else {
            return Base64.decode(data, flags);
        }
    }

    public static String base64Str(boolean encrypt, byte[] data, int flags) {
        if (encrypt) {
            return Base64.encodeToString(data, flags);
        } else {
            return Base64.encodeToString(data, flags);
        }
    }
}
```

### 5.CRC32校验

CRC32常用于数据校验，它没有MD5,SHA算法复杂，性能开销非常小。CRC是模-2除法的余数，采用的除数不同，CRC的类型也就不一样。通常，CRC的除数用生成多项式来表示。最常用的CRC码的生成多项式如表1所示。最常用的CRC码及生成多项式名称生成多项式  
**CRC-12:** $x^{12}=x^{11}+x^3+x^2+x+1$  
**CRC-16:** $x^{16}+x^{15}+x^2+1$  
**CRC-CCITT:** $x^{16}+x^{12}+x^5+1$  
**CRC-32:** $x^{32}+x^{26}+x^{23}+x^{22}+x^{16}+x^{12}+x^{11}+x^{10}+x^8+x^7+x^5+x^4+x^2+x+1$  
java中已实现CRC32算法，我们封装一下就可以用了，代码如下:

```java
public class Demo {
    /**
     * CRC32校验码.
     * @param bytes 待校验的数据.
     * @return 校验码
     */
    public static long crc32(byte[] bytes) {
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        return crc32.getValue();
    }
}
```

还有一种CRC实现方式为查表法，我们建立一个自己的表，使用轮询处理，表不同得到的校验码就不同，示例如下:

```java
public class CRC32 {
    private final static int TABLE[] = {
            0x00000000, 0x04c11db7, 0x09823b6e, 0x0d4326d9, 0x130476dc, 0x17c56b6b, 0x1a864db2, 0x1e475005,
            0x2608edb8, 0x22c9f00f, 0x2f8ad6d6, 0x2b4bcb61, 0x350c9b64, 0x31cd86d3, 0x3c8ea00a, 0x384fbdbd,
            0x4c11db70, 0x48d0c6c7, 0x4593e01e, 0x4152fda9, 0x5f15adac, 0x5bd4b01b, 0x569796c2, 0x52568b75,
            0x6a1936c8, 0x6ed82b7f, 0x639b0da6, 0x675a1011, 0x791d4014, 0x7ddc5da3, 0x709f7b7a, 0x745e66cd,
            0x9823b6e0, 0x9ce2ab57, 0x91a18d8e, 0x95609039, 0x8b27c03c, 0x8fe6dd8b, 0x82a5fb52, 0x8664e6e5,
            0xbe2b5b58, 0xbaea46ef, 0xb7a96036, 0xb3687d81, 0xad2f2d84, 0xa9ee3033, 0xa4ad16ea, 0xa06c0b5d,
            0xd4326d90, 0xd0f37027, 0xddb056fe, 0xd9714b49, 0xc7361b4c, 0xc3f706fb, 0xceb42022, 0xca753d95,
            0xf23a8028, 0xf6fb9d9f, 0xfbb8bb46, 0xff79a6f1, 0xe13ef6f4, 0xe5ffeb43, 0xe8bccd9a, 0xec7dd02d,
            0x34867077, 0x30476dc0, 0x3d044b19, 0x39c556ae, 0x278206ab, 0x23431b1c, 0x2e003dc5, 0x2ac12072,
            0x128e9dcf, 0x164f8078, 0x1b0ca6a1, 0x1fcdbb16, 0x018aeb13, 0x054bf6a4, 0x0808d07d, 0x0cc9cdca,
            0x7897ab07, 0x7c56b6b0, 0x71159069, 0x75d48dde, 0x6b93dddb, 0x6f52c06c, 0x6211e6b5, 0x66d0fb02,
            0x5e9f46bf, 0x5a5e5b08, 0x571d7dd1, 0x53dc6066, 0x4d9b3063, 0x495a2dd4, 0x44190b0d, 0x40d816ba,
            0xaca5c697, 0xa864db20, 0xa527fdf9, 0xa1e6e04e, 0xbfa1b04b, 0xbb60adfc, 0xb6238b25, 0xb2e29692,
            0x8aad2b2f, 0x8e6c3698, 0x832f1041, 0x87ee0df6, 0x99a95df3, 0x9d684044, 0x902b669d, 0x94ea7b2a,
            0xe0b41de7, 0xe4750050, 0xe9362689, 0xedf73b3e, 0xf3b06b3b, 0xf771768c, 0xfa325055, 0xfef34de2,
            0xc6bcf05f, 0xc27dede8, 0xcf3ecb31, 0xcbffd686, 0xd5b88683, 0xd1799b34, 0xdc3abded, 0xd8fba05a,
            0x690ce0ee, 0x6dcdfd59, 0x608edb80, 0x644fc637, 0x7a089632, 0x7ec98b85, 0x738aad5c, 0x774bb0eb,
            0x4f040d56, 0x4bc510e1, 0x46863638, 0x42472b8f, 0x5c007b8a, 0x58c1663d, 0x558240e4, 0x51435d53,
            0x251d3b9e, 0x21dc2629, 0x2c9f00f0, 0x285e1d47, 0x36194d42, 0x32d850f5, 0x3f9b762c, 0x3b5a6b9b,
            0x0315d626, 0x07d4cb91, 0x0a97ed48, 0x0e56f0ff, 0x1011a0fa, 0x14d0bd4d, 0x19939b94, 0x1d528623,
            0xf12f560e, 0xf5ee4bb9, 0xf8ad6d60, 0xfc6c70d7, 0xe22b20d2, 0xe6ea3d65, 0xeba91bbc, 0xef68060b,
            0xd727bbb6, 0xd3e6a601, 0xdea580d8, 0xda649d6f, 0xc423cd6a, 0xc0e2d0dd, 0xcda1f604, 0xc960ebb3,
            0xbd3e8d7e, 0xb9ff90c9, 0xb4bcb610, 0xb07daba7, 0xae3afba2, 0xaafbe615, 0xa7b8c0cc, 0xa379dd7b,
            0x9b3660c6, 0x9ff77d71, 0x92b45ba8, 0x9675461f, 0x8832161a, 0x8cf30bad, 0x81b02d74, 0x857130c3,
            0x5d8a9099, 0x594b8d2e, 0x5408abf7, 0x50c9b640, 0x4e8ee645, 0x4a4ffbf2, 0x470cdd2b, 0x43cdc09c,
            0x7b827d21, 0x7f436096, 0x7200464f, 0x76c15bf8, 0x68860bfd, 0x6c47164a, 0x61043093, 0x65c52d24,
            0x119b4be9, 0x155a565e, 0x18197087, 0x1cd86d30, 0x029f3d35, 0x065e2082, 0x0b1d065b, 0x0fdc1bec,
            0x3793a651, 0x3352bbe6, 0x3e119d3f, 0x3ad08088, 0x2497d08d, 0x2056cd3a, 0x2d15ebe3, 0x29d4f654,
            0xc5a92679, 0xc1683bce, 0xcc2b1d17, 0xc8ea00a0, 0xd6ad50a5, 0xd26c4d12, 0xdf2f6bcb, 0xdbee767c,
            0xe3a1cbc1, 0xe760d676, 0xea23f0af, 0xeee2ed18, 0xf0a5bd1d, 0xf464a0aa, 0xf9278673, 0xfde69bc4,
            0x89b8fd09, 0x8d79e0be, 0x803ac667, 0x84fbdbd0, 0x9abc8bd5, 0x9e7d9662, 0x933eb0bb, 0x97ffad0c,
            0xafb010b1, 0xab710d06, 0xa6322bdf, 0xa2f33668, 0xbcb4666d, 0xb8757bda, 0xb5365d03, 0xb1f740b4
    };

    private static int reverse(int data, int bits) {
        int result = 0;
        for (int i = 0; i < bits; i++) {
            result += ((data >> i) & 1) << (bits - 1 - i);
        }
        return result;
    }

    public static int crc32(byte[] data) {
        int crc32 = 0xffffffff;
        for (byte aData : data) {
            crc32 = (crc32 << 8) ^ TABLE[(reverse(aData, 8) ^ (crc32 >> 24)) & 0xFF];
        }
        return reverse(~crc32, 32);
    }
}
```

### 6.扩展

我们做应用程序经常会与嵌入式的程序通讯，在安全性不高又想快捷开发，这个时候我们可以自己写一些简单的加密算法，我们拿到字节码后，可以按字节码或按位进行与，或，非，或者异或处理，在处理传输过程中需要注意java与C语言的字节码大小端对齐问题。C语言中区分有符号整型和无符号整型，而java中只有有符号整型。这些在数据加密与传输过程中都需要多注意一下。
