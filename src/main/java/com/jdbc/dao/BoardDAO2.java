package com.jdbc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.jdbc.dto.BoardDTO;

public class BoardDAO2 {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) throws Exception {
		this.jdbcTemplate = jdbcTemplate;

	}

	Connection conn = null;

	public int getMaxNum() {

		int maxNum = 0;
		StringBuilder sql = new StringBuilder(100);

		sql.append("select nvl(max(num),0) from board");

		maxNum = jdbcTemplate.queryForInt(sql.toString());

		return maxNum;

	}

	public void insertData(BoardDTO dto) {

		StringBuilder sql = new StringBuilder(200);

		sql.append("insert into board (num,name,pwd,email,subject,content,");
		sql.append("ipAddr,hitCount,created) ");
		sql.append("values(?,?,?,?,?,?,?,0,sysdate)");

		jdbcTemplate.update(sql.toString(), dto.getNum(), dto.getName(), dto.getPwd(), dto.getEmail(), dto.getSubject(),
				dto.getContent(), dto.getIpAddr());
	}

	public List<BoardDTO> getList(int start, int end, String searchKey, String searchValue) {

		StringBuilder sql = new StringBuilder(500);

		searchValue = "%" + searchValue + "%";

		sql.append("select * from (");
		sql.append("select rownum rnum,data.* from(");
		sql.append("select num,name,subject,hitCount,");
		sql.append("to_char(created,'YYYY-MM-DD') created ");
		sql.append("from board where " + searchKey + " like ? order by num desc) data) ");
		sql.append("where rnum >= ? and rnum <= ?");

		List<BoardDTO> lists = jdbcTemplate.query(sql.toString(), new Object[] { searchValue, start, end },
				new RowMapper<BoardDTO>() {

					@Override
					public BoardDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

						BoardDTO dto = new BoardDTO();
						dto.setNum(rs.getInt("num"));
						dto.setName(rs.getString("name"));
						dto.setSubject(rs.getString("subject"));
						dto.setHitCount(rs.getInt("hitCount"));
						dto.setCreated(rs.getString("created"));

						return dto;
					}

				});

		return lists;

	}

	public int getDataCount(String searchKey, String searchValue) {

		int result = 0;

		StringBuilder sql = new StringBuilder(200);

		searchValue = "%" + searchValue + "%";

		sql.append("select nvl(count(*),0) from board ");
		sql.append("where " + searchKey + " like ?");

		result = jdbcTemplate.queryForInt(sql.toString(), searchValue);

		return result;

	}

	public void updateHitCount(int num) {

		StringBuilder sql = new StringBuilder(200);

		sql.append("update board set hitCount=hitCount + 1 where num=?");

		jdbcTemplate.update(sql.toString(), num);

	}

	public BoardDTO getReadData(int num) {

		StringBuilder sql = new StringBuilder();

		sql.append("select num,name,pwd,email,subject,content,ipAddr,");
		sql.append("hitCount,created from board where num=?");

		BoardDTO dtoOne = jdbcTemplate.queryForObject(sql.toString(), new RowMapper<BoardDTO>() {

			@Override
			public BoardDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

				BoardDTO dto = new BoardDTO();

				dto.setNum(rs.getInt("num"));
				dto.setName(rs.getString("name"));
				dto.setPwd(rs.getString("pwd"));
				dto.setEmail(rs.getString("email"));
				dto.setSubject(rs.getString("subject"));
				dto.setContent(rs.getString("content"));
				dto.setIpAddr(rs.getString("ipAddr"));
				dto.setHitCount(rs.getInt("hitCount"));
				dto.setCreated(rs.getString("created"));

				return dto;
			}

		}, num);

		return dtoOne;
	}

	public void deleteData(int num) {

		StringBuilder sql = new StringBuilder();

		sql.append("delete board where num=?");

		jdbcTemplate.update(sql.toString(), num);
	}

	public void updateData(BoardDTO dto) {

		StringBuilder sql = new StringBuilder();

		sql.append("update board set name=?, pwd=?, email=?, subject=?,");
		sql.append("content=? where num=?");
		
		jdbcTemplate.update(sql.toString(),dto.getName(),dto.getPwd(),dto.getEmail(),dto.getSubject(),dto.getContent(),dto.getNum());

}

}
