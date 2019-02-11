
package cn.e3mall.service;

import java.util.List;

import cn.e3mall.common.pojo.EasyUITreeNode;

/**  

* <p>Title: ItemcatService</p>  

* <p>Description: </p>  

* @author zty  

* @date 2018年9月29日  

*/
public interface ItemcatService {
	//类目分类树
	List<EasyUITreeNode> getIteamcatlist(long parentId);
	
}
