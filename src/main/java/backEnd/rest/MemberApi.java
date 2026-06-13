package backEnd.rest;

import backEnd.dao.MemberDao;
import backEnd.dao.impl.MemberDaoImpl;
import backEnd.entity.Member;
import backEnd.util.PasswordUtil;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;

@Path("/members")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MemberApi {

    private final MemberDao memberDao = new MemberDaoImpl();

    @GET
    public List<MemberDto> findAll() {
        return memberDao.findAll().stream().map(MemberDto::from).toList();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {
        return memberDao.findById(id)
                .map(member -> Response.ok(MemberDto.from(member)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).entity(Message.error("會員不存在")).build());
    }

    @POST
    public Response create(MemberRequest request) {
        validateCreate(request);
        if (memberDao.existsByUsername(request.username)) {
            return Response.status(Response.Status.CONFLICT).entity(Message.error("username 已存在")).build();
        }

        Member member = new Member();
        member.setName(request.name);
        member.setUsername(request.username);
        member.setPasswordHash(PasswordUtil.hash(request.password));
        member.setEmail(emptyToNull(request.email));
        member.setAddress(request.address);
        member.setPhone(request.phone);
        member.setRole(valueOrDefault(request.role, "USER"));
        member.setStatus(valueOrDefault(request.status, "ACTIVE"));

        Member saved = memberDao.save(member);
        return Response.status(Response.Status.CREATED).entity(MemberDto.from(saved)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, MemberRequest request) {
        return memberDao.findById(id).map(member -> {
            member.setName(request.name);
            member.setUsername(request.username);
            if (request.password != null && !request.password.isBlank()) {
                member.setPasswordHash(PasswordUtil.hash(request.password));
            }
            member.setEmail(emptyToNull(request.email));
            member.setAddress(request.address);
            member.setPhone(request.phone);
            member.setRole(valueOrDefault(request.role, "USER"));
            member.setStatus(valueOrDefault(request.status, "ACTIVE"));
            Member updated = memberDao.update(member);
            return Response.ok(MemberDto.from(updated)).build();
        }).orElse(Response.status(Response.Status.NOT_FOUND).entity(Message.error("會員不存在")).build());
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        if (memberDao.findById(id).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(Message.error("會員不存在")).build();
        }
        memberDao.delete(id);
        return Response.ok(Message.ok("刪除成功")).build();
    }

    private void validateCreate(MemberRequest request) {
        if (request == null || isBlank(request.name) || isBlank(request.username) || isBlank(request.password)) {
            throw new IllegalArgumentException("name, username, password 為必填");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static class MemberRequest {
        public String name;
        public String username;
        public String password;
        public String email;
        public String address;
        public String phone;
        public String role;
        public String status;
    }

    public static class MemberDto {
        public Long id;
        public String name;
        public String username;
        public String email;
        public String address;
        public String phone;
        public String role;
        public String status;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        public static MemberDto from(Member member) {
            MemberDto dto = new MemberDto();
            dto.id = member.getId();
            dto.name = member.getName();
            dto.username = member.getUsername();
            dto.email = member.getEmail();
            dto.address = member.getAddress();
            dto.phone = member.getPhone();
            dto.role = member.getRole();
            dto.status = member.getStatus();
            dto.createdAt = member.getCreatedAt();
            dto.updatedAt = member.getUpdatedAt();
            return dto;
        }
    }

    public static class Message {
        public boolean success;
        public String message;

        public static Message ok(String message) {
            Message result = new Message();
            result.success = true;
            result.message = message;
            return result;
        }

        public static Message error(String message) {
            Message result = new Message();
            result.success = false;
            result.message = message;
            return result;
        }
    }
}
