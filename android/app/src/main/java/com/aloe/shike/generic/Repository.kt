package com.aloe.shike.generic

import com.aloe.http.IHttp
import javax.inject.Inject

class Repository @Inject constructor(http:IHttp):IHttp by http

