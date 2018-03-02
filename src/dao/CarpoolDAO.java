package dao;

import pojo.BookedCarpoolInfo;
import pojo.CarpoolInfo;
import pojo.DateTime;
import pojo.VehicleOwnerInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by admin on 2017/8/15.
 */
public class CarpoolDAO extends AbstractDAO<CarpoolInfo>{
    private static CarpoolDAO instance = new CarpoolDAO();
    public static CarpoolDAO getInstance(){
        return instance;
    }
    private CarpoolDAO(){}
    public List<CarpoolInfo> searchAvaliableVehicle(String from, String to, int passenger, String date){
        List<CarpoolInfo> carpoolInfoList = new ArrayList<>();
        try {
            Connection connection = ConnectionPool.getInstance().getCarpoolConnection();
            String sqlquery = "select * from CARPOOL where departure = ? AND  destination = ? AND remainseat >= ? AND date LIKE ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlquery);
            preparedStatement.setString(1,from);
            preparedStatement.setString(2,to);
            preparedStatement.setString(3,Integer.toString(passenger));
            preparedStatement.setString(4,date+"%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                carpoolInfoList.add(parseCursor(resultSet));
            }
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return carpoolInfoList;
    }

    public boolean storageCarpoolInfo(String departure, String destination,String capacity,String price,String date, String userid){
        try{
            Connection connection = ConnectionPool.getInstance().getCarpoolConnection();
            String sqlquery = "INSERT INTO CARPOOL(uid,date,price,capacity,departure,destination,remainseat) VALUES(?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlquery);
            preparedStatement.setString(1,userid);
            preparedStatement.setString(2,date);
            preparedStatement.setString(3,price);
            preparedStatement.setString(4,capacity);
            preparedStatement.setString(5,departure);
            preparedStatement.setString(6,destination);
            preparedStatement.setString(7,capacity);
            preparedStatement.execute();
            preparedStatement.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<BookedCarpoolInfo> searchAllBookedCarpool(int uid){
        return searchAllBookedCarpool(Integer.toString(uid));
    }
    public List<BookedCarpoolInfo> searchAllBookedCarpool(String uid){
        List<BookedCarpoolInfo> infoList = new ArrayList<>();
        try {
            Connection connection = ConnectionPool.getInstance().getCarpoolConnection();
            String sql = "SELECT * FROM BOOKED_CARPOOL WHERE uid = ? ORDER BY booking_ref DESC";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,uid);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                String refnumber = resultSet.getString(1);
                String id = resultSet.getString(resultSet.findColumn("id"));
                int seat = resultSet.getInt(4);
                CarpoolInfo carpoolInfo = getCarpoolInfo(id);
                BookedCarpoolInfo bookedCarpoolInfo = new BookedCarpoolInfo(carpoolInfo,refnumber,seat);
                infoList.add(bookedCarpoolInfo);
            }
            preparedStatement.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return infoList;
    }
    public boolean bookCarpool(String uid, String id,String seat_string){
        /*
            1.获取剩余座位数
            2.判断如果超过剩余座位，则不进行预订
            3.更新剩余座位
            4.在carpool_book中加入详情
         */
        boolean flag = false;
        int seats = Integer.parseInt(seat_string);
        try {
            Connection connection = ConnectionPool.getInstance().getCarpoolConnection();
            String sqlquery = "SELECT remainseat FROM CARPOOL where id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlquery);
            preparedStatement.setString(1,id);
            ResultSet resultSet = preparedStatement.executeQuery();
            int remainseat = 0;
            if (resultSet.next()){
                remainseat = resultSet.getInt(1);
            }
            preparedStatement.close();
            if(remainseat>=seats){
                sqlquery = "UPDATE carpool SET remainseat = ? WHERE id = ?";
                preparedStatement = connection.prepareStatement(sqlquery);
                preparedStatement.setInt(1,remainseat-seats);
                preparedStatement.setString(2,id);
                preparedStatement.execute();
                preparedStatement.close();
                if (BookedCarpoolDAO.getInstance().BookCarpool(id,uid,seat_string)){
                    flag = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }
    public CarpoolInfo getCarpoolInfo(String id){
        CarpoolInfo carpoolInfo = null;
        try {
            Connection connection = ConnectionPool.getInstance().getCarpoolConnection();
            String sqlquery = "SELECT * FROM CARPOOL WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlquery);
            preparedStatement.setString(1,id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
               carpoolInfo = parseCursor(resultSet);
            }
            preparedStatement.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return carpoolInfo;
    }
    @Override
    protected CarpoolInfo parseCursor(ResultSet resultSet) {
        try {
            int id = resultSet.getInt(resultSet.findColumn("id"));
            VehicleOwnerInfo vehicleOwnerInfo = VehicleOwnerInfoDAO.getInstance().getUserInfo(resultSet.getInt(resultSet.findColumn("uid")));
            DateTime dateTime = new DateTime(resultSet.getString(resultSet.findColumn("date")));
            int price = resultSet.getInt(resultSet.findColumn("price"));
            int capacity = resultSet.getInt(resultSet.findColumn("capacity"));
            int remainseat = resultSet.getInt(resultSet.findColumn("remainseat"));
            String departure = resultSet.getString(resultSet.findColumn("departure"));
            String dest = resultSet.getString(resultSet.findColumn("destination"));
            return new CarpoolInfo(vehicleOwnerInfo,id,price,capacity,dateTime,remainseat,departure,dest);
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
    public ArrayList<CarpoolInfo> getPostedCarpool(String uid){
        String sqlquery = "SELECT * FROM CARPOOL WHERE uid = ?";
        ArrayList<CarpoolInfo> list = new ArrayList<>();
        try {
            Connection connection = ConnectionPool.getInstance().getCarpoolConnection();
            PreparedStatement preparedStatement =connection.prepareStatement(sqlquery);
            preparedStatement.setString(1,uid);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                list.add(parseCursor(resultSet));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return list;
    }

    public List<CarpoolInfo> getCarpoolInfoAsDriver(String uid) {
        LinkedList<CarpoolInfo> allinfos = new LinkedList<>();
        String sqlquery = "SELECT * FROM CARPOOL WHERE uid = ?";
        try {
            PreparedStatement preparedStatement = ConnectionPool.getInstance().getCarpoolConnection().prepareStatement(sqlquery);
            preparedStatement.setString(1, uid);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int cid = resultSet.getInt(resultSet.findColumn("id"));
                String departure = resultSet.getString(resultSet.findColumn("departure"));
                String destination = resultSet.getString(resultSet.findColumn("destination"));
                int price = resultSet.getInt(resultSet.findColumn("price"));
                int seat = resultSet.getInt(resultSet.findColumn("capacity"));
                int remainseat = resultSet.getInt(resultSet.findColumn("remainseat"));
                String data_str = resultSet.getString(resultSet.findColumn("date"));
                DateTime date = new DateTime(data_str);
                CarpoolInfo carpoolInfo = new CarpoolInfo();
                carpoolInfo.setCapacity(seat);
                carpoolInfo.setId(cid);
                carpoolInfo.setFrom(departure);
                carpoolInfo.setTo(destination);
                carpoolInfo.setPrice(price);
                carpoolInfo.setRemainseat(remainseat);
                carpoolInfo.setDateTime(date);
                allinfos.add(carpoolInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return allinfos;
    }

    public boolean addCarpool(CarpoolInfo carpoolInfo){
        boolean flag = false;
        String sqlquery = "INSERT INTO CARPOOL(uid, date, price, capacity, departure, destination, remainseat) VALUES (?,?,?,?,?,?,?)";
        try {
            PreparedStatement preparedStatement = ConnectionPool.getInstance().getCarpoolConnection().prepareStatement(sqlquery);
            preparedStatement.setInt(1,carpoolInfo.getUser().getUid());
            preparedStatement.setString(2,carpoolInfo.getDateTime().toString());
            preparedStatement.setInt(3,carpoolInfo.getPrice());
            preparedStatement.setInt(4,carpoolInfo.getCapacity());
            preparedStatement.setString(5,carpoolInfo.getDeparture());
            preparedStatement.setString(6,carpoolInfo.getDestination());
            preparedStatement.setInt(7,carpoolInfo.getCapacity());
            preparedStatement.execute();
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }
    public boolean driverVerify(String uid, String carpoolid){
        boolean flag = false;
        String sqlquery =  "SELECT COUNT(*) FROM CARPOOL WHERE uid = ? AND id = ?";
        try {
            PreparedStatement preparedStatement = ConnectionPool.getInstance().getCarpoolConnection().prepareStatement(sqlquery);
            preparedStatement.setString(1,uid);
            preparedStatement.setString(2,carpoolid);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            if(resultSet.getInt(1)==1){
                flag = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }
}
