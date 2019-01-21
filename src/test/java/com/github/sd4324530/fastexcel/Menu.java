package com.github.sd4324530.fastexcel;

import com.github.sd4324530.fastexcel.entity.DBMenu;
import com.github.sd4324530.fastexcel.entity.MenuObject;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * 1
 */

public class Menu {

    private static final Logger LOG = LoggerFactory.getLogger(MenuObject.class);
    Connection connection;
    PreparedStatement ps;
    List<MenuObject> menus;

    HashMap<String, HashMap<String, Integer>> cache = new HashMap<String, HashMap<String, Integer>>();

    @Test
    public void test() throws ClassNotFoundException, SQLException, IOException, InvalidFormatException {

        connection = getConnection();
        connection.setAutoCommit(false);

        menus = createExcel();

        doMenu();

    }

    private void doMenu() throws SQLException {

        try {

            int c = 0;
            for (MenuObject menu : menus) {
                Integer level1Id = insertMenu(menu.level1, -1);
                Integer level2Id = insertMenu(menu.level2, level1Id);
                Integer level3Id = insertMenu(menu.level3, level2Id);
                Integer level4Id = insertMenu(menu.level4, level3Id);

                connection.commit();

                c++;
                LOG.debug(String.valueOf(c));
            }

        } catch (Exception e) {
            e.printStackTrace();
            connection.rollback();
        } finally {
            ps.close();
            connection.close();
        }


    }

    private Integer insertMenu(String menu, Integer parentId) throws SQLException {

        if (null == menu || "null".equals(menu) || null == parentId) {
            return null;
        }

        // TODO insert
        DBMenu dbMenu = getDBMenu(menu, parentId);
        int menuId = 999999;

        if (null != dbMenu.getId()) {
            // LOG.debug("【" + menu + "】 已经存在。");
            menuId = dbMenu.getId();
        } else {
            LOG.debug("增加菜单【" + menu + "】 。");
            ps = connection.prepareStatement("insert into base_menu (code, title, parent_id, order_num, path, crt_time, crt_user, crt_name, crt_host, upd_time, upd_user, upd_name, upd_host) values (?, ?, ?, 0, '/null', '2018-12-17 19:00:00', '1', 'admin', '127.0.0.1', '2018-12-17 19:00:00', '1', 'admin', '127.0.0.1')");

            ps.setString(1, "bookingManagement");
            ps.setString(2, menu);
            ps.setInt(3, parentId);

            ps.executeUpdate();

            menuId = getDBMenu(menu, parentId).getId();

            // add Relationship
            addRel(menuId);
        }

        return menuId;
    }

    private void addRel(int menuId) throws SQLException {

        // 19 服务商DMS管理员
        ps = connection.prepareStatement("insert into base_resource_authority (authority_id, authority_type, resource_id, resource_type, parent_id) values (?, 'group', ?, 'menu', -1)");
        ps.setInt(1, 19);
        ps.setInt(2, menuId);
        ps.executeUpdate();

        // 77 主机厂DMS管理员
        ps = connection.prepareStatement("insert into base_resource_authority (authority_id, authority_type, resource_id, resource_type, parent_id) values (?, 'group', ?, 'menu', -1)");
        ps.setInt(1, 77);
        ps.setInt(2, menuId);
        ps.executeUpdate();

        // 1 DMS管理员
        ps = connection.prepareStatement("insert into base_resource_authority (authority_id, authority_type, resource_id, resource_type, parent_id) values (?, 'group', ?, 'menu', -1)");
        ps.setInt(1, 1);
        ps.setInt(2, menuId);

        ps.executeUpdate();

    }

    private DBMenu getDBMenu(String menu, Integer parent_id) throws SQLException {

        ps = connection.prepareStatement("select * from base_menu where title = ? and parent_id = ?");
        ps.setString(1, menu);
        ps.setInt(2, parent_id);
        ResultSet rs = ps.executeQuery();
        DBMenu dbMenu = new DBMenu();

        int c = 0;
        while(rs.next()){
            dbMenu.setId(rs.getInt(1));
            c++;
        }

        if (1 < c) {
            // LOG.debug("【" + menu + "】 发生了重复。");
            throw new RuntimeException("【" + menu + "】 发生了重复。");
        }

        return dbMenu;
    }

    private List<MenuObject> createExcel() throws IOException, InvalidFormatException {
        FastExcel fastExcel = new FastExcel("D:\\downloads\\完整菜单结构.xlsx");
        fastExcel.setSheetName("上线计划");
        List<MenuObject> menus = fastExcel.parse(MenuObject.class);

        if (null != menus && !menus.isEmpty()) {
            for (MenuObject myTest : menus) {
                LOG.debug("记录:{}", myTest.toString());
            }

//            FastExcel create = new FastExcel("D:\\downloads\\shadowsocks\\完整菜单结构.xlsx");
//            create.setSheetName("活动信息数据");
//            boolean result = create.createExcel(menus);
//            LOG.debug("结果:{}", result);
//            create.close();
        } else {
            LOG.debug("没有结果");
        }
        fastExcel.close();

        return menus;
    }

    public Connection getConnection() throws SQLException {
        Connection connection;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://172.17.171.62:3306/ag_admin_v1?useUnicode=true&characterEncoding=UTF-8", "myuser", "mypassword");
            System.out.println("成功连接数据库");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("class not find !", e);
        } catch (SQLException e) {
            throw new RuntimeException("get connection error!", e);
        }

        return connection;
    }
}
