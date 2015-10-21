package com.dao;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.item.PersonItem;
import com.sun.org.apache.bcel.internal.generic.RETURN;

public class SqlHelper {
	public boolean addPerson(PersonItem person) {
		String personId = person.getPersonId();
		String name = person.getName();
		String sex = person.getSex();
		String address = person.getAddress();
		String cardId = person.getCardId();
		String headUrl = person.getHeadUrl();
		
		String sql = "insert into person (personId,name,sex,address,cardId,headUrl) values('"
		+personId+"','"+name+"','"+sex+"','"+address+"','"+
				cardId+"','"+headUrl+"')";
		
		boolean ISSUCCESS = false;
		try {
			int result = SqlUtils.excuteUpdate(sql);
			if(result>0){
				ISSUCCESS = true;	
			}else {
				ISSUCCESS = false;		
			}
			System.out.println("add person ...");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ISSUCCESS;
	}
	public boolean deletePerson(PersonItem person){
		String personId = person.getPersonId();
		String name = person.getName();
		
		String sql = "delete from person where personId='"+personId+"' and name='"+name+"'";
		
		boolean ISSUCCESS = false;
		try{
			int result = SqlUtils.excuteUpdate(sql);
			if(result>0){
				ISSUCCESS = true;
			}else {
				ISSUCCESS = false;
			}
			System.out.println("delete person ...");
		}catch(Exception e){}
		return ISSUCCESS;
	}
	
	public boolean updatePerson(PersonItem person){
		String sql = "update person set name='"+person.getName()+
				"',sex='"+person.getSex()+
				"',address='"+person.getAddress()+
				"',cardId='"+person.getCardId()+
				"',headUrl='"+person.getHeadUrl()+
				"' where personId='"+person.getPersonId()+"'";
		boolean ISSUCEESS = false;
		try {
			int result = SqlUtils.excuteUpdate(sql);
			if(result>0){
				ISSUCEESS = true;
			}else {
				ISSUCEESS = false;
			}
			System.out.println("update person ...");
		} catch (Exception e) {
			// TODO: handle exception
		}
		return ISSUCEESS;
	}
	public PersonItem findPerson(int id){
		PersonItem person =null;
		String sql = "select * from person where id="+id;
		try {
			java.sql.ResultSet rs = SqlUtils.executeQuery(sql);
			while(rs.next()){
				person = new PersonItem();
				person.setPersonId(rs.getString(2));
				person.setName(rs.getString(3));
				person.setSex(rs.getString(4));
				person.setAddress(rs.getString(5));
				person.setCardId(rs.getString(6));
				person.setHeadUrl(rs.getString(7));
			}
			System.out.println("find person ...");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return person;
	}
	public PersonItem findPerson(String personId){
		PersonItem person =null;
		String sql = "select * from person where personId='"+personId+"'";
		try {
			java.sql.ResultSet rs = SqlUtils.executeQuery(sql);
			while(rs.next()){
				person = new PersonItem();
				person.setPersonId(rs.getString(2));
				person.setName(rs.getString(3));
				person.setSex(rs.getString(4));
				person.setAddress(rs.getString(5));
				person.setCardId(rs.getString(6));
				person.setHeadUrl(rs.getString(7));
			}
			System.out.println("find person ...");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return person;
	}
	public int size(){
		int size = 0;
		ResultSet rs = null;
		String sql = "select count(*) from person";
		try{
			 rs = SqlUtils.executeQuery(sql);
			while(rs.next()){
				size = rs.getInt(1);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return size;
	}
	public ResultSet findHead() {
		ResultSet rs = null;
		String sql = "select id,headUrl from person";
		try{
			rs = SqlUtils.executeQuery(sql);
		}catch(Exception e){
			e.printStackTrace();
		}
		return rs;
	}
	
}
