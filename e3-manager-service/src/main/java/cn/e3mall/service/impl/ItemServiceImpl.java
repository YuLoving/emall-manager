
package cn.e3mall.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.pojo.EasyUlSDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.IDUtils;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.mapper.TbItemDescMapper;
import cn.e3mall.mapper.TbItemMapper;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbItemDesc;
import cn.e3mall.pojo.TbItemExample;
import cn.e3mall.service.ItemService;

/**  

* <p>Title: ItemServiceImpl</p>  

* <p>Description: </p>  

* @author zty  

* @date 2018年7月27日  

*/
@Service
public class ItemServiceImpl implements ItemService {
	  
	@Autowired
	private TbItemMapper tbItemMapper;
	@Autowired
	private TbItemDescMapper tbItemDescMapper;
	
	//mq发送消息,需要注入 jmsTemplate、topicDestination对象
	@Autowired
	private JmsTemplate jmsTemplate;
	@Resource
	private Destination topicDestination;
	
	//使用缓存
	@Autowired
	private JedisClient jedisClient;
	
	//取配置文件中的信息 key的前缀
	@Value("${REDIS_ITEM_PER}")
	private String REDIS_ITEM_PER;
	//取配置文件中的信息 过期时间
	@Value("${ITEM_CACHE_EXPIRE}")
	private Integer ITEM_CACHE_EXPIRE;
	
	
	
	private int insert2;
	
	@Override
	public TbItem getItemById(long itemid) {
		//第一步，查询有没有缓存，有的话直接取缓存里的数据
		try {
			String json = jedisClient.get(REDIS_ITEM_PER+":"+itemid+":BASE");
			if(StringUtils.isNotBlank(json)){
				TbItem tbItem = JsonUtils.jsonToPojo(json, TbItem.class);
				return tbItem;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//缓存中没有则查询数据库。
		TbItem item = tbItemMapper.selectByPrimaryKey(itemid);
		//查询结果不是空，就添加到缓存中
		if(item!=null && !"".equals(item)){
			try {
				jedisClient.set(REDIS_ITEM_PER+":"+itemid+":BASE", JsonUtils.objectToJson(item));
				//设置过期时间，比如一个小时即3600s
				jedisClient.expire(REDIS_ITEM_PER+":"+itemid+":BASE", ITEM_CACHE_EXPIRE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return item;
	}

	
	@Override
	public EasyUlSDataGridResult getList(int page, int rows) {
		//设置分页信息
		PageHelper.startPage(page, rows);
		//执行查询
		TbItemExample itemExample = new TbItemExample();
		List<TbItem> list = tbItemMapper.selectByExample(itemExample);
		//创建一个返回值对象
		EasyUlSDataGridResult result = new EasyUlSDataGridResult();
		result.setRows(list);
		//取分页结果
		PageInfo<TbItem> pageInfo = new PageInfo<>(list);
		long total = pageInfo.getTotal();
		result.setTotal(total);
		
		return result;
	}


	
	@Override
	public E3Result addItem(TbItem item, String desc) {
		//生成商品ID
		final long itemId = IDUtils.genItemId();
		//补全item的属性
		item.setId(itemId);
		item.setStatus((byte) 1);//1-正常，2-下架，3-删除
		item.setCreated(new Date());
		item.setUpdated(new Date());
		//向商品表插入数据
		tbItemMapper.insert(item);
		//创建一个商品描述表对应的pojo对象
		TbItemDesc itemDesc = new TbItemDesc();
		//补全属性desc
		itemDesc.setItemId(itemId);
		itemDesc.setItemDesc(desc);
		itemDesc.setCreated(new Date());
		itemDesc.setUpdated(new Date());
		//向商品描述表插入数据
		 tbItemDescMapper.insert(itemDesc);
		 
		 	//发送添加商品这个消息
		 jmsTemplate.send(topicDestination, new MessageCreator() {
			
			@Override
			public Message createMessage(Session session) throws JMSException {
				TextMessage message = session.createTextMessage(itemId+"");
				return message;
			}
		});
		 
		//返回成功
		return E3Result.ok();
	}


	/* 
	 * 编辑商品
	 */
	@Override
	public E3Result editItem(TbItem item, String desc) {
		
		//补全属性
		item.setStatus((byte) 1);
		item.setCreated(new Date());
		item.setUpdated(new Date());
		//更新商品表
		tbItemMapper.updateByPrimaryKey(item);
		//创建一个商品描述表对应的pojo对象
		TbItemDesc itemDesc = new TbItemDesc();
		//补全属性desc
		itemDesc.setItemId(item.getId());
		itemDesc.setItemDesc(desc);
		itemDesc.setCreated(new Date());
		itemDesc.setUpdated(new Date());
		//更新商品描述表
		tbItemDescMapper.updateByPrimaryKeySelective(itemDesc);
		//返回成功
		return E3Result.ok();
	}


	/*
	 * 删除商品
	 */
	@Override
	public E3Result deleItem(long id) {
		
		tbItemMapper.deleteByPrimaryKey(id);
		
		tbItemDescMapper.deleteByPrimaryKey(id);
		return E3Result.ok();
	}


	/* 
	 * 上架商品
	 */
	@Override
	public E3Result upjiaItem(long id) {
		TbItem item = new TbItem();
		item.setId(id);
		item.setStatus((byte) 1);
		item.setUpdated(new Date());
		tbItemMapper.updateByPrimaryKeySelective(item);
		return E3Result.ok();
	}


	/* 
	 * 下架商品
	 */
	@Override
	public E3Result downjiaItem(long id) {
		TbItem item = new TbItem();
		item.setId(id);
		item.setStatus((byte) 2);
		item.setUpdated(new Date());
		tbItemMapper.updateByPrimaryKeySelective(item);
		return E3Result.ok();
	}


	/*   
	 * 根据商品id查询商品描述
	 */
	@Override
	public TbItemDesc getItemDescbyid(long itemId) {
		//第一步，查询有没有缓存，有的话直接取缓存里的数据
				try {
					String json = jedisClient.get(REDIS_ITEM_PER+":"+itemId+":DESC");
					if(StringUtils.isNotBlank(json)){
						TbItemDesc tbItemDesc = JsonUtils.jsonToPojo(json, TbItemDesc.class);
						return tbItemDesc;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		TbItemDesc itemDesc = tbItemDescMapper.selectByPrimaryKey(itemId);
		try {
			jedisClient.set(REDIS_ITEM_PER+":"+itemId+":DESC", JsonUtils.objectToJson(itemDesc));
			//设置过期时间，比如一个小时即3600s
			jedisClient.expire(REDIS_ITEM_PER+":"+itemId+":DESC", ITEM_CACHE_EXPIRE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemDesc;
	}

}
