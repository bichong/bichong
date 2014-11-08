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

public class Return {

private String result;
private String sign;


public String getResult() {

return result;

}

public void setResult(String name) {

this.result = name;

}

public String getSign() {

return sign;

}

public void setSign(String name) {

this.sign = name;

}

@Override
public String toString() {
	return result+"&"+sign;
}

} 
