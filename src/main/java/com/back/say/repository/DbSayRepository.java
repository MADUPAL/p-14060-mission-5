package com.back.say.repository;

import com.back.say.domain.Say;
import com.back.say.dto.PageDto;
import com.back.say.dto.SayDto;
import com.back.say.utils.Pageable;
import com.back.say.utils.SaySearchCondition;
import com.back.standard.util.DbConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DbSayRepository implements SayRepository{
    @Override
    public int create(SayDto dto) {
        String sql = "INSERT INTO say (content, author) VALUES (?, ?)";

        try(Connection conn = DbConnectionUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, dto.getContent());
            pstmt.setString(2, dto.getAuthor());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("INSERT 실패, 반영된 row 없음");
            }

            try(ResultSet rs = pstmt.getGeneratedKeys()) {
                if(rs.next()) {
                    return rs.getInt(1);
                }
                throw new RuntimeException("INSERT: 생성된 ID를 가져오는 데 실패");
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB INSERT 실패", e);
        }
    }

    @Override
    public int update(int id, SayDto dto) {
        String sql = "UPDATE say SET content = ?, author = ? WHERE id = ?";

        try(Connection conn = DbConnectionUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dto.getContent());
            pstmt.setString(2, dto.getAuthor());
            pstmt.setInt(3, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows != 0 ? id : -1;


        } catch (SQLException e) {
            throw new RuntimeException("DB UPDATE 실패", e);
        }
    }

    @Override
    public int delete(int id) {
        String sql = "DELETE FROM say WHERE id = ?";

        try(Connection conn = DbConnectionUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows != 0 ? id : -1;

        } catch (SQLException e) {
            throw new RuntimeException("DB DELETE 실패", e);
        }
    }

    @Override
    public Optional<Say> findById(int id) {
        String sql = "SELECT id, content, author FROM say WHERE id = ?";

        try(Connection conn = DbConnectionUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if(!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB SELECT (findById) 실패", e);
        }
    }

    @Override
    public List<Say> findAll() {
        String sql = "SELECT id, content, author FROM say ORDER BY id DESC";

        try(Connection conn = DbConnectionUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ArrayList<Say> sayList = new ArrayList<>();

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sayList.add(mapRow(rs));
                }
            }

            return sayList;

        } catch (SQLException e) {
            throw new RuntimeException("DB SELECT (findAll) 실패", e);
        }
    }

    @Override
    public void build() {

    }

    @Override
    public PageDto<Say> findPage(SaySearchCondition cond, Pageable pageable) {
        StringBuilder fromClause = new StringBuilder("FROM say");
        List<Object> params = new ArrayList<>();
        addWhereToSql(cond, fromClause, params);

        int count = count(fromClause, params);
        List<Say> pageContent = findPageContent(fromClause, params, pageable);

        return new PageDto<>(pageContent, pageable.getPageNo(), pageable.getPageSize(), count);
    }

    private List<Say> findPageContent(StringBuilder fromClause, List<Object> params, Pageable pageable) {
        String sql = "SELECT id, content, author " + fromClause + " ORDER BY id DESC LIMIT ? OFFSET ?";
        System.out.println("sql = " + sql);
        List<Say> result = new ArrayList<>();

        try(Connection conn = DbConnectionUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            bindParams(params, pstmt);
            int idx = params.size();
            idx++;
            pstmt.setInt(idx++, pageable.getPageSize());
            pstmt.setInt(idx, pageable.getOffset());

            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) {
                    result.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB findPageContent 실패", e);
        }

        return result;
    }

    private int count(StringBuilder fromClause, List<Object> params) {
        String sql = "SELECT COUNT(*) " + fromClause;

        try(Connection conn = DbConnectionUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            bindParams(params, pstmt);

            try(ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB COUNT(*) 실패", e);
        }

    }

    private void bindParams(List<Object> params, PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            pstmt.setObject(i+1, params.get(i));
        }
    }

    private void addWhereToSql(SaySearchCondition cond, StringBuilder sql, List<Object> params) {
        if ((!cond.hasAuthorCondition() || cond.getAuthorContains().isBlank()) &&
                (!cond.hasContentCondition() || cond.getContentContains().isBlank())) {
            return;
        }
        sql.append(" WHERE ");

        if (cond.hasAuthorCondition() && cond.hasContentCondition()) {
            sql.append("(author LIKE ? OR content LIKE ?)");
            params.add("%"+cond.getAuthorContains()+"%");
            params.add("%"+cond.getContentContains()+"%");
        } else if (cond.hasAuthorCondition()) {
            sql.append("author LIKE ?");
            params.add("%"+cond.getAuthorContains()+"%");
        } else if (cond.hasContentCondition()) {
            sql.append("content LIKE ?");
            params.add("%"+cond.getContentContains()+"%");
        }
    }

    private Say mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String content = rs.getString("content");
        String author = rs.getString("author");
        return new Say(id, author, content);
    }
}
