package org.eclipse.wst.jsdt.internal.oaametadata;

public class Author {
//	author_element = element author {
//		  author_content  &  author_attributes  &  foreign_attributes
//		}
//		author_content = (
//		  aboutMe_element?  &  quote_element?
//		)
//		author_attributes = (
//		  email?  &  location?  &  name?  &  organization?  &  
//		  photo?  &  type?  &  website?
//		)
	
	public String email;
	public String location;
	public String name;
	public String organization;
	public String photo;
	public String type;
	public String website;

	public String aboutMe;
	public String quote;
}
