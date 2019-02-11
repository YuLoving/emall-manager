
package cn.e3mall.service;

import cn.e3mall.common.pojo.EasyUlSDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbItemDesc;

/**  

* <p>Title: ItemService</p>  

* <p>Description: </p>  

* @author zty  

* @date 2018年7月27日  

*/
public interface ItemService {

	public TbItem getItemById(long itemid);
	//分页
	public EasyUlSDataGridResult getList(int page,int rows);
	
	//添加商品接口
	public E3Result addItem(TbItem item,String desc);
	
	//编辑商品接口
	public E3Result editItem(TbItem item,String desc);
	
	//删除商品
	public E3Result deleItem(long id);
	
	//下架商品
	public E3Result upjiaItem(long id);
	//上架商品
	public E3Result downjiaItem(long id);
	
	//根据商品id查询商品描述
	public TbItemDesc getItemDescbyid(long itemId);
	
	
}
