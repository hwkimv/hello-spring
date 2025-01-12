package hello.hellospring.repository;

import hello.hellospring.domain.Member;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMemberRepository implements MemberRepository {

    private final DataSource dataSource;  // 데이터베이스 연결을 위한 DataSource 객체

    public JdbcMemberRepository(DataSource dataSource) {
        this.dataSource = dataSource;  // 생성자를 통해 DataSource 객체를 주입받음
    }

    @Override
    public Member save(Member member) {
        String sql = "insert into member(name) values(?)";  // 회원 정보를 저장할 SQL 쿼리
        Connection conn = null;  // 데이터베이스 연결
        PreparedStatement pstmt = null;  // 쿼리 실행을 위한 PreparedStatement
        ResultSet rs = null;  // 결과를 담을 ResultSet

        try {
            conn = getConnection();  // 데이터베이스 연결
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);  // 쿼리 준비
            pstmt.setString(1, member.getName());  // 회원 이름을 쿼리에 바인딩
            pstmt.executeUpdate();  // 쿼리 실행
            rs = pstmt.getGeneratedKeys();  // 자동 생성된 키(회원 ID)를 가져옴

            if (rs.next()) {
                member.setId(rs.getLong(1));  // 생성된 회원 ID 설정
            } else {
                throw new SQLException("id 조회 실패");  // ID 조회 실패 시 예외 발생
            }

            return member;  // 저장된 회원 객체 반환
        } catch (Exception e) {
            throw new IllegalStateException(e);  // 예외 발생 시 상태 예외로 감싸서 던짐
        } finally {
            close(conn, pstmt, rs);  // 자원 해제
        }
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "select * from member where id = ?";  // 회원 ID로 검색하는 SQL 쿼리
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();  // 데이터베이스 연결
            pstmt = conn.prepareStatement(sql);  // 쿼리 준비
            pstmt.setLong(1, id);  // 회원 ID를 쿼리에 바인딩
            rs = pstmt.executeQuery();  // 쿼리 실행

            if (rs.next()) {
                Member member = new Member();  // 조회된 결과를 Member 객체로 매핑
                member.setId(rs.getLong("id"));
                member.setName(rs.getString("name"));
                return Optional.of(member);  // 조회된 회원 객체를 Optional로 반환
            } else {
                return Optional.empty();  // 회원이 없으면 빈 Optional 반환
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);  // 예외 발생 시 상태 예외로 감싸서 던짐
        } finally {
            close(conn, pstmt, rs);  // 자원 해제
        }
    }

    @Override
    public List<Member> findAll() {
        String sql = "select * from member";  // 모든 회원을 조회하는 SQL 쿼리
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();  // 데이터베이스 연결
            pstmt = conn.prepareStatement(sql);  // 쿼리 준비
            rs = pstmt.executeQuery();  // 쿼리 실행
            List<Member> members = new ArrayList<>();  // 회원 리스트 생성

            while (rs.next()) {
                Member member = new Member();  // 조회된 결과를 Member 객체로 매핑
                member.setId(rs.getLong("id"));
                member.setName(rs.getString("name"));
                members.add(member);  // 리스트에 추가
            }

            return members;  // 모든 회원 리스트 반환
        } catch (Exception e) {
            throw new IllegalStateException(e);  // 예외 발생 시 상태 예외로 감싸서 던짐
        } finally {
            close(conn, pstmt, rs);  // 자원 해제
        }
    }

    @Override
    public Optional<Member> findByName(String name) {
        String sql = "select * from member where name = ?";  // 이름으로 회원을 조회하는 SQL 쿼리
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();  // 데이터베이스 연결
            pstmt = conn.prepareStatement(sql);  // 쿼리 준비
            pstmt.setString(1, name);  // 회원 이름을 쿼리에 바인딩
            rs = pstmt.executeQuery();  // 쿼리 실행

            if (rs.next()) {
                Member member = new Member();  // 조회된 결과를 Member 객체로 매핑
                member.setId(rs.getLong("id"));
                member.setName(rs.getString("name"));
                return Optional.of(member);  // 조회된 회원 객체를 Optional로 반환
            }

            return Optional.empty();  // 회원이 없으면 빈 Optional 반환
        } catch (Exception e) {
            throw new IllegalStateException(e);  // 예외 발생 시 상태 예외로 감싸서 던짐
        } finally {
            close(conn, pstmt, rs);  // 자원 해제
        }
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);  // DataSource에서 연결을 가져옴
    }

    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();  // ResultSet 닫기
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (pstmt != null) {
                pstmt.close();  // PreparedStatement 닫기
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (conn != null) {
                close(conn);  // Connection 닫기
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void close(Connection conn) throws SQLException {
        DataSourceUtils.releaseConnection(conn, dataSource);  // DataSource에서 연결을 반환
    }
}

