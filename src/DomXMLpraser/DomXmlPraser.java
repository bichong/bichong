/*
* Copyright 2013-2014 the original author or authors.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package DomXMLpraser;
import java.io.InputStream;

import java.util.ArrayList;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import org.w3c.dom.Element;

import org.w3c.dom.Node;

import org.w3c.dom.NodeList;

import android.util.Log;


public class DomXmlPraser {

public static Return readXML(InputStream inStream) 
	{

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Return jieguo = new Return();
		try {

			DocumentBuilder builder = factory.newDocumentBuilder();

			Document dom = builder.parse(inStream);

			Element root = dom.getDocumentElement();

			NodeList items = root.getElementsByTagName("result");//��������person�ڵ�
		

			//�õ���һ��person�ڵ�

			Element personNode = (Element) items.item(0);
		
			//��ȡperson�ڵ��µ������ӽڵ�(��ǩ֮��Ŀհ׽ڵ��name/ageԪ��)

			jieguo.setResult(personNode.getFirstChild().getNodeValue());
			NodeList items2 = root.getElementsByTagName("sign");//��������person�ڵ�
		

			//�õ���һ��person�ڵ�

			personNode = (Element) items2.item(0);
		
			//��ȡperson�ڵ��µ������ӽڵ�(��ǩ֮��Ŀհ׽ڵ��name/ageԪ��)

			jieguo.setSign(personNode.getFirstChild().getNodeValue());


   
			inStream.close();

			} catch (Exception e) {

				e.printStackTrace();
			}

			return jieguo;

		}
public static String readXML2(InputStream inStream) 
{
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	String jie ="";
	String rate="";
	String adress="";
	try {
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document dom = builder.parse(inStream);
		Element root = dom.getDocumentElement();
		NodeList items = root.getElementsByTagName("result");//��������person�ڵ�
		//�õ���һ��person�ڵ�
		Element personNode = (Element) items.item(0);	
		//��ȡperson�ڵ��µ������ӽڵ�(��ǩ֮��Ŀհ׽ڵ��name/ageԪ��)
		jie=personNode.getFirstChild().getNodeValue();
		NodeList items1 = root.getElementsByTagName("rate");//��������person�ڵ�
		//�õ���һ��person�ڵ�
		Element personNode1 = (Element) items1.item(0);	
		//��ȡperson�ڵ��µ������ӽڵ�(��ǩ֮��Ŀհ׽ڵ��name/ageԪ��)
		rate=personNode1.getFirstChild().getNodeValue();
		NodeList items2 = root.getElementsByTagName("adress");//��������person�ڵ�
		//�õ���һ��person�ڵ�
		Element personNode2 = (Element) items2.item(0);
		if(personNode2!=null)
			//��ȡperson�ڵ��µ������ӽڵ�(��ǩ֮��Ŀհ׽ڵ��name/ageԪ��)
			adress=personNode2.getFirstChild().getNodeValue();
		inStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return jie+","+rate+","+adress;
	}
} 
