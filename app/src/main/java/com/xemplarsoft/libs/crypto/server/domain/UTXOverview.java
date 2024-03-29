package com.xemplarsoft.libs.crypto.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UTXOverview extends Entity {
	@JsonProperty("txid")
	private String txId;
	@JsonProperty("vout")
	private Integer vOut;

	public UTXOverview() {}
	public UTXOverview(String txId, int vOut){
		this.txId = txId;
		this.vOut = vOut;
	}

	public void setTxId(String tx){
	    this.txId = tx;
    }

    public void setVOut(Integer vOut){
	    this.vOut = vOut;
    }

    public String getTxId(){
		return txId;
	}

	public int getVout(){
		return vOut;
	}

	public String toString(){
		return txId;
	}
}