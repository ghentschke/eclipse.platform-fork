<%--
 Copyright (c) 2000, 2007 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="header.jsp"%>


<jsp:include page="toolbar.jsp">
	<jsp:param name="script" value="navActions.js"/>
	<jsp:param name="view" value="search"/>

	<jsp:param name="name"     value="show_all"/>
	<jsp:param name="tooltip"  value='show_all'/>
	<jsp:param name="image"    value="show_all.gif"/>
	<jsp:param name="action"   value="toggleShowAll"/>
	<jsp:param name="param"    value=""/>
	<jsp:param name="state"    value="<%=(new ActivitiesData(application, request, response)).getButtonState()%>"/>

	<jsp:param name="name"     value="show_categories"/>
	<jsp:param name="tooltip"  value='show_categories'/>
	<jsp:param name="image"    value="show_categories.gif"/>
	<jsp:param name="action"   value="toggleShowCategories"/>
	<jsp:param name="param"    value=""/>
	<jsp:param name="state"    value="<%=((new SearchData(application, request, response)).isShowCategories() ? "on" : "off")%>"/>

	<jsp:param name="name"     value="show_descriptions"/>
	<jsp:param name="tooltip"  value='show_descriptions'/>
	<jsp:param name="image"    value="show_descriptions.gif"/>
	<jsp:param name="action"   value="toggleShowDescriptions"/>
	<jsp:param name="param"    value=""/>
	<jsp:param name="state"    value="<%=((new SearchData(application, request, response)).isShowDescriptions() ? "on" : "off")%>"/>

	<jsp:param name="name"     value="synchnav"/>
	<jsp:param name="tooltip"  value='SynchNav'/>
	<jsp:param name="image"    value="synch_nav.gif"/>
	<jsp:param name="action"   value="resynchNav"/>
	<jsp:param name="param"    value=""/>
	<jsp:param name="state"    value='off'/>
</jsp:include>