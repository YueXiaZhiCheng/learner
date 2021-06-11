package com.dataprocess.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析xml
 *
 * @Author north xin
 * @Date 10:41 2021/5/12
 */

public class XmlUtil {

    static Logger log = LoggerFactory.getLogger(XmlUtil.class);


//    public static void main(String[] args) throws Exception {
//
////        org.bson.Document document = xml2Document(new File("D:\\workFiles\\20210427三重一大数据对接\\示例数据\\91110000100018267J_0006_1000_20210413181449_2021041318144976482586680032E184\\0006_1000_20210120_75EBA9B30A537C76482586680032E184.xml"));
//        org.bson.Document document = xml2Document(new File("D:\\workFiles\\20210427三重一大数据对接\\示例数据\\91110000100018267J_0016_1000_20210428144943_20210428144943F8482584D4000E353B\\0016_1000_20171211_A73F28EF8E08F1F8482584D4000E353B.xml"));
////        org.bson.Document document = xml2Document(new File("C:\\Users\\xin'bei'bei\\Desktop\\无标题-4.txt"));
//
//        System.out.println(document);
//    }

    /**
     * xml文件直接转document
     *
     * @param xmlFile
     * @return
     * @throws Exception
     */
    public static org.bson.Document xml2Document(File xmlFile) throws Exception {
        if (!xmlFile.exists())
            return null;
        StringBuilder builder = null;
        BufferedReader bufferedReader = null;
        try {
            builder = new StringBuilder();
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
            String temp;
            while ((temp = bufferedReader.readLine()) != null) {
                builder.append(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage() + ExceptionUtil.getStackTraceString(e));
        } finally {
            bufferedReader.close();
        }
        if (builder == null || StringUtils.isBlank(builder.toString()))
            return null;
        return xml2Document(builder.toString());
    }

    /**
     * xml 字符串变document
     *
     * @param xmlStr
     * @return
     * @throws Exception
     */
    public static org.bson.Document xml2Document(String xmlStr) throws Exception {
        //System.out.println("我是xml数据" + xmlStr);
        if (StringUtils.isBlank(xmlStr))
            return null;

        log.info("待解析xml数据 : {}", xmlStr);
        Document document = DocumentHelper.parseText(xmlStr);
        Element rootElement = document.getRootElement();
        org.bson.Document reDoc = handleElement(rootElement);
        return reDoc;
    }

    private static org.bson.Document handleElement(Element rootElement) throws Exception {
        org.bson.Document reDoc = new org.bson.Document();
        List<Element> elements = rootElement.elements();
        if (CollectionUtils.isNotEmpty(elements)) {
            for (Element element : elements) {
                handleOneElement(reDoc, element);
            }
        }
        return reDoc;
    }

    private static List<Object> handleListElement(Element rootElement) throws Exception {
        List<Object> reDoc = new ArrayList<>();
        List<Element> elements = rootElement.elements();
        if (CollectionUtils.isNotEmpty(elements)) {
            for (Element element : elements) {
                org.bson.Document document1 = new org.bson.Document();
                handleOneElement(document1, element);
                document1.forEach((s, o) -> {
                    reDoc.add(o);
                });
            }
        }
        return reDoc;
    }


    private static void handleOneElement(org.bson.Document reDoc, Element element) throws Exception {
        List<Element> elements1 = element.elements();

        String name = element.getName();

        if (name.endsWith("_list")) {
            List<Object> documents = handleListElement(element);
            Object o = reDoc.get(name);
            if (o == null) {
                reDoc.append(name, documents);
            } else {
                documents.addAll((List<Object>) o);
                reDoc.put(name, documents);
            }
        } else {
            if (CollectionUtils.isNotEmpty(elements1)) {
                //有子标签
                org.bson.Document document = handleElement(element);
                Object o = reDoc.get(name);
                if (o == null) {
                    reDoc.append(name, document);
                } else {
                    List<org.bson.Document> strings;
                    if (o instanceof org.bson.Document) {
                        strings = new ArrayList<>();
                        org.bson.Document string = (org.bson.Document) o;
                        strings.add(string);
                    } else {
                        strings = (List<org.bson.Document>) o;
                    }
                    strings.add(document);
                    reDoc.put(name, strings);
                }
            } else {
                //没有子标签
                Object o = reDoc.get(name);
                if (o == null) {
                    reDoc.append(name, element.getStringValue());
                } else {
                    List<String> strings;
                    if (o instanceof String) {
                        strings = new ArrayList<>();
                        String string = (String) o;
                        strings.add(string);
                    } else {
                        strings = (List<String>) o;
                    }
                    strings.add(element.getStringValue());
                    reDoc.put(name, strings);
                }
            }
        }
    }


//原版,全部解析
//    private static org.bson.Document handleElement1(Element rootElement) throws Exception {
//        org.bson.Document reDoc = new org.bson.Document();
//        List<Element> elements = rootElement.elements();
//        if (CollectionUtils.isNotEmpty(elements)) {
//            for (Element element : elements) {
//                List<Element> elements1 = element.elements();
//                String name = element.getName();
//                if (CollectionUtils.isNotEmpty(elements1)) {
//                    //有子标签
//                    org.bson.Document document = handleElement1(element);
//                    Object o = reDoc.get(name);
//                    if (o == null) {
//                        reDoc.append(name, document);
//                    } else {
//                        List<org.bson.Document> strings;
//                        if (o instanceof org.bson.Document) {
//                            strings = new ArrayList<>();
//                            org.bson.Document string = (org.bson.Document) o;
//                            strings.add(string);
//                        } else {
//                            strings = (List<org.bson.Document>) o;
//                        }
//                        strings.add(document);
//                        reDoc.put(name, strings);
//                    }
//                } else {
//                    //没有子标签
//                    Object o = reDoc.get(name);
//                    if (o == null) {
//                        reDoc.append(name, element.getStringValue());
//                    } else {
//                        List<String> strings;
//                        if (o instanceof String) {
//                            strings = new ArrayList<>();
//                            String string = (String) o;
//                            strings.add(string);
//                        } else {
//                            strings = (List<String>) o;
//                        }
//                        strings.add(element.getStringValue());
//                        reDoc.put(name, strings);
//                    }
//                }
//            }
//        }
//        return reDoc;
//    }
}
