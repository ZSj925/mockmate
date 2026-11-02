package zsj.mockmate.mapper;

import org.apache.ibatis.annotations.Mapper;
import zsj.mockmate.pojo.entity.UserEntity;

@Mapper
public interface authMapper {


    UserEntity getByusername(String username);

}
