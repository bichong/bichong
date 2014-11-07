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
