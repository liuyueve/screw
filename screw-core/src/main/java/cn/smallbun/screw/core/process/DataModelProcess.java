/*
 * screw-core - 简洁好用的数据库表结构文档生成工具
 * Copyright © 2020 SanLi (qinggang.zuo@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cn.smallbun.screw.core.process;

import cn.smallbun.screw.core.Configuration;
import cn.smallbun.screw.core.engine.EngineFileType;
import cn.smallbun.screw.core.metadata.Database;
import cn.smallbun.screw.core.metadata.model.ColumnModel;
import cn.smallbun.screw.core.metadata.model.DataModel;
import cn.smallbun.screw.core.metadata.model.TableModel;
import cn.smallbun.screw.core.query.DatabaseQuery;
import cn.smallbun.screw.core.query.DatabaseQueryFactory;
import cn.smallbun.screw.core.util.BeanUtils;

import java.util.List;

import static cn.smallbun.screw.core.util.BeanUtils.*;
import static cn.smallbun.screw.core.util.BeanUtils.beanAttributeValueReplaceBlank;

/**
 * 数据模型处理
 *
 * @author SanLi
 * Created by qinggang.zuo@gmail.com / 2689170096@qq.com on 2020/3/22 21:12
 */
public class DataModelProcess extends AbstractProcess<DataModel> {

    private Configuration config;

    /**
     * 构造方法
     *
     * @param configuration {@link Configuration}
     */
    public DataModelProcess(Configuration configuration) {
        super(configuration.getProduceConfig());
        this.config = configuration;
    }

    /**
     * 处理
     *
     * @return {@link DataModel}
     */
    @Override
    public DataModel process() {
        //获取query对象
        DatabaseQuery query = new DatabaseQueryFactory(config.getDataSource()).newInstance();
        DataModel model = new DataModel();
        //Title
        model.setTitle(config.getTitle());
        //org
        model.setOrganization(config.getOrganization());
        //org url
        model.setOrganizationUrl(config.getOrganizationUrl());
        //version
        model.setVersion(config.getVersion());
        //description
        model.setDescription(config.getDescription());

        /*查询操作开始*/
        long start = System.currentTimeMillis();
        //获取数据库
        Database database = query.getDataBase();
        logger.debug("query the database time consuming:{}ms",
            (System.currentTimeMillis() - start));
        model.setDatabase(database.getDatabase());
        //获取表的信息
        List<TableModel> tableModels = getTableModel(config.getDataSource());
        //设置表
        model.setTables(tableModels);
        //优化数据
        optimizeData(model);
        /*封装数据结束*/
        logger.debug("encapsulation processing data time consuming:{}ms",
            (System.currentTimeMillis() - start));
        return model;
    }

    /**
     * 优化数据
     *
     * @param dataModel {@link DataModel}
     * @see "1.0.3"
     */
    private void optimizeData(DataModel dataModel) {
        //trim
        beanAttributeValueTrim(dataModel);
        //tables
        List<TableModel> tables = dataModel.getTables();
        //columns
        tables.forEach(i -> {
            //table escape xml
            beanAttributeValueTrim(i);
            List<ColumnModel> columns = i.getColumns();
            //columns escape xml
            columns.forEach(BeanUtils::beanAttributeValueTrim);
        });
        //if file type is word
        if (config.getEngineConfig().getFileType().equals(EngineFileType.WORD)) {
            //escape xml
            beanAttributeValueEscapeXml(dataModel);
            //tables
            tables.forEach(i -> {
                //table escape xml
                beanAttributeValueEscapeXml(i);
                List<ColumnModel> columns = i.getColumns();
                //columns escape xml
                columns.forEach(BeanUtils::beanAttributeValueEscapeXml);
            });
        }
        //if file type is markdown
        if (config.getEngineConfig().getFileType().equals(EngineFileType.MD)) {
            //escape xml
            beanAttributeValueReplaceBlank(dataModel);
            //columns
            tables.forEach(i -> {
                //table escape xml
                beanAttributeValueReplaceBlank(i);
                List<ColumnModel> columns = i.getColumns();
                //columns escape xml
                columns.forEach(BeanUtils::beanAttributeValueReplaceBlank);
            });
        }
    }

}
