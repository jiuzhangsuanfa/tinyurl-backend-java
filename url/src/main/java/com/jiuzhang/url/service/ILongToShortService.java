package com.jiuzhang.url.service;

import com.jiuzhang.url.vo.UrlVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @auther: WZ
 * @Date: 2020/9/7 14:57
 * @Description:
 */
public interface ILongToShortService {

    UrlVO longToShort(String longUrl, HttpServletRequest request);

    UrlVO longToShort(String longUrl);

    String shortToLong(String shortUrl);
}
