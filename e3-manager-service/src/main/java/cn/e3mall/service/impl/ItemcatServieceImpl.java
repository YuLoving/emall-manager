/**
 * 
 */
package cn.e3mall.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.e3mall.common.pojo.EasyUITreeNode;
import cn.e3mall.mapper.TbItemCatMapper;
import cn.e3mall.pojo.TbItemCat;
import cn.e3mall.pojo.TbItemCatExample;
import cn.e3mall.pojo.TbItemCatExample.Criteria;
import cn.e3mall.service.ItemcatService;


/**  

* <p>Title: ItemcatServieceImpl</p>  

* <p>Description: s商品分类管理</p>  

* @author zty  

* @date 2018年9月29日  

*/
@Service
public class ItemcatServieceImpl implements ItemcatService {
	@Autowired
	private  TbItemCatMapper itemCatMapper;
	
	@Override
	public List<EasyUITreeNode> getIteamcatlist(long parentId) {
			//通过parentId查询子节点类表
			TbItemCatExample example = new TbItemCatExample();
			Criteria criteria = example.createCriteria();//创建条件对象
			criteria.andParentIdEqualTo(parentId);//设置查询条件
			List<TbItemCat> list = itemCatMapper.selectByExample(example);
			
			//把列表转换成EasyUITreeNode列表
			List<EasyUITreeNode> resultlist=new ArrayList<>();
			for(TbItemCat itemCat:list ){
				EasyUITreeNode node = new EasyUITreeNode();
				//设置属性
				node.setId(itemCat.getId());
				node.setText(itemCat.getName());
				node.setState(itemCat.getIsParent()?"closed":"open");
				resultlist.add(node);
			}
			//返回结果
		return resultlist;
	}

}
