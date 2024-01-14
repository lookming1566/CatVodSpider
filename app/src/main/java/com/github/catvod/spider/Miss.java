package com.github.catvod.spider;

import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Filter;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Miss extends Spider {


    private final String url = "https://missav.com/";
    private  final String cnUrl = url + "cn/";

//    @Override
//    public String homeContent(boolean filter) throws Exception {
//        List<Vod> list = new ArrayList<>();
//        List<Class> classes = new ArrayList<>();
//        LinkedHashMap<String, List<Filter>> filters = new LinkedHashMap<>();
//        Document doc = Jsoup.parse(OkHttp.string(cnUrl));
//        for (Element a : doc.select("a.block.px-4.py-2.text-sm.leading-5.text-nord5.bg-nord3")) {
//
//            String typeId = a.attr("href").replace(url, "");
//            if (typeId.startsWith("dm") || typeId.contains("VR") || typeId.startsWith("cn")) {
//                classes.add(new Class(typeId, a.text()));
//                filters.put(typeId, Arrays.asList(new Filter("filters", "過濾", Arrays.asList(new Filter.Value("全部", ""), new Filter.Value("單人作品", "individual"), new Filter.Value("中文字幕", "chinese-subtitle")))));
//            }
//        }
//        for (Element div : doc.select("div.thumbnail.group")) {
//            String id = div.select("a.text-secondary").attr("href").replace(url, "");
//            String name = div.select("a.text-secondary").text();
//            String pic = div.select("img").attr("data-src");
//            if (pic.isEmpty()) pic = div.select("img").attr("src");
//            String remark = div.select("span").text();
//            if (TextUtils.isEmpty(name)) continue;
//            list.add(new Vod(id, name, pic, remark));
//        }
//        return Result.string(classes, list, filters);
//    }
    @Override
    public String homeContent(boolean filter) throws Exception {
        List<Vod> list = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        LinkedHashMap<String, List<Filter>> filters = new LinkedHashMap<>();
        Document doc = Jsoup.parse(OkHttp.string(cnUrl));
        for (Element a : doc.select("a.block.px-4.py-2.text-sm.leading-5.text-nord5.bg-nord3")) {

            String typeId = a.attr("href").replace(url, "");
            if (typeId.startsWith("dm") || typeId.contains("VR") || typeId.startsWith("cn")) {
                classes.add(new Class(typeId, a.text()));
                filters.put(typeId, Arrays.asList(new Filter("filters", "過濾", Arrays.asList(new Filter.Value("全部", ""), new Filter.Value("單人作品", "individual"), new Filter.Value("中文字幕", "chinese-subtitle")))));
            }
        }
        if (doc.select("div.thumbnail.group").isEmpty()){
            for (Element ul : doc.select("li").eq(0).select("div.space-y-4")) {
                String id = ul.select("a.text-nord13").eq(1).attr("href").replace(url, "");
                String name = ul.select("h4.text-nord13.truncate").eq(1).text();
                String pic = ul.select("img").attr("data-src");
                if (pic.isEmpty()) pic = ul.select("img").attr("src");
                String remark = ul.select("p.text-nord10").eq(0).text();
                if (TextUtils.isEmpty(name)) continue;
                list.add(new Vod(id, name, pic, remark));
            }

        }else {
            for (Element div : doc.select("div.thumbnail.group")) {
                String id = div.select("a.text-secondary").attr("href").replace(url, "");
                String name = div.select("a.text-secondary").text();
                String pic = div.select("img").attr("data-src");
                if (pic.isEmpty()) pic = div.select("img").attr("src");
                String remark = div.select("span").text();
                if (TextUtils.isEmpty(name)) continue;
                list.add(new Vod(id, name, pic, remark));
            }
        }




        return Result.string(classes, list, filters);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        List<Vod> list = new ArrayList<>();
        String target =url + tid + "?page=" + pg ;
        SpiderDebug.log(target);
        String filters = extend.get("filters");
        if (TextUtils.isEmpty(filters)) {
            target += "?page=" + pg;
        } else {
            // 如果 filters 不为空，将 "?filters=" + extend.get("filters") + "&page=" + pg" 追加到目标 URL 中
            target += "?filters=" + extend.get("filters") + "page=" + pg;
        }

        // 使用 Jsoup 解析目标 URL 的网页内容
        Document doc = Jsoup.parse(OkHttp.string(target));
        for (Element div : doc.select("div.thumbnail")) {
            String id = div.select("a.text-secondary").attr("href").replace(url, "");
            String name = div.select("a.text-secondary").text();
            String pic = div.select("img").attr("data-src");
            if (pic.isEmpty()) pic = div.select("img").attr("src");
            String remark = div.select("span").text();
            if (TextUtils.isEmpty(name)) continue;
            list.add(new Vod(id, name, pic, remark));
        }


        //翻页
        int page = Integer.parseInt(pg), limit = 12, total = 0;
        return Result.get().vod(list).page(page, 2000, limit, total).toString();
    }


    @Override
    public String detailContent(List<String> ids) throws Exception {
        Document doc = Jsoup.parse(OkHttp.string(url + ids.get(0)));
        String name = doc.select("meta[property=og:title]").attr("content");
        String pic = doc.select("meta[property=og:image]").attr("content");
        Vod vod = new Vod();
        vod.setVodId(ids.get(0));
        vod.setVodPic(pic);
        vod.setVodName(name);
        vod.setVodPlayFrom("MissAV");
        vod.setVodPlayUrl("播放$" + ids.get(0));
        return Result.string(vod);
    }



    @Override
    public String searchContent(String key, boolean quick) throws Exception {
//        return searchContent(key, "1");
        String[] parts = key.split("\\|"); // 使用中文问号进行分隔
        String leftPart="";
        String rightPart="";
        if (parts.length >= 2) {
            leftPart = parts[0];
            rightPart = parts[1];
            return searchContent(leftPart, rightPart);
        } else {
            System.out.println("无法找到'？'分隔符");
            return searchContent(key, "1");
        }

    }


    @Override
    public String searchContent(String key, boolean quick, String pg) throws Exception {
//        return searchContent(key, pg);
        List<Vod> list = new ArrayList<>();
        String target =cnUrl + "search/" + key + "?page=" + pg;
        Document doc = Jsoup.parse(OkHttp.string(target));
        for (Element div : doc.select("div.thumbnail")) {
            String id = div.select("a.text-secondary").attr("href").replace(url, "");
            String name = div.select("a.text-secondary").text();
            String pic = div.select("img").attr("data-src");
            if (pic.isEmpty()) pic = div.select("img").attr("src");
            String remark = div.select("span").text();
            if (TextUtils.isEmpty(name)) continue;
            list.add(new Vod(id, name, pic, remark));
        }
//        return Result.string(list);
        //翻页
        int page = Integer.parseInt(pg), limit = 18, total = 0;
        return Result.get().vod(list).page(page, 2000, limit, total).toString();
    }


    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        return Result.get().parse().url(url + id).string();
    }


    private String searchContent(String key, String pg) {
        List<Vod> list = new ArrayList<>();
        String target =cnUrl + "search/" + key + "?page=" + pg;
        Document doc = Jsoup.parse(OkHttp.string(target));
        for (Element div : doc.select("div.thumbnail")) {
            String id = div.select("a.text-secondary").attr("href").replace(url, "");
            String name = div.select("a.text-secondary").text();
            String pic = div.select("img").attr("data-src");
            if (pic.isEmpty()) pic = div.select("img").attr("src");
            String remark = div.select("span").text();
            if (TextUtils.isEmpty(name)) continue;
            list.add(new Vod(id, name, pic, remark));
        }
//        return Result.string(list);
        //翻页
        int page = Integer.parseInt(pg), limit = 18, total = 0;
        return Result.get().vod(list).page(page, 2000, limit, total).toString();
    }
}
