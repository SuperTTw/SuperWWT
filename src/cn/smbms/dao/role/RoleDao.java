package cn.smbms.dao.role;

import java.sql.Connection;
import java.util.List;
import cn.smbms.entiy.Role;

public interface RoleDao {
	
	public List<Role> getRoleList(Connection connection)throws Exception;

}
