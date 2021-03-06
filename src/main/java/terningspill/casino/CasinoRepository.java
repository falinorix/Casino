package terningspill.casino;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CasinoRepository {

    @Autowired
    private JdbcTemplate db;

    Logger logger = LoggerFactory.getLogger(CasinoRepository.class);

    public int rullTerning(){
        try{
            int rull =  (int)(Math.random() * 6 ) + 1;
            return rull;

        }catch (Exception e){
            logger.error("Feil i rullTerning : " + e);
            return -1;
        }

    }

    public boolean lagreResultat(Spiller spiller){
        String sql = "INSERT INTO Spiller (brukernavn, telefonnr, land, by, terningkast) VALUES (?,?,?,?,?);";
        try{
            db.update(sql, spiller.getBrukernavn(), spiller.getTelefonnr(), spiller.getLand(), spiller.getBy(), spiller.getTerningKast());
            return true;
        }catch (Exception e){
            logger.error("Feil i lagreResultat : " + e);
            return false;
        }

    }

    public List<Spiller> hentHighscore(){
        String sql = "SELECT * FROM Spiller ORDER BY terningKast DESC;";
        try{
            List<Spiller> highscore = db.query(sql, new BeanPropertyRowMapper<>(Spiller.class));
            return highscore;

        }catch (Exception e){
            logger.error("Feil i hentHighscore" + e);
            return null;
        }

    }

    public boolean slettHighscore(){
        String sql = "DELETE FROM Spiller;";
        try {
            db.update(sql);
            return true;

        }catch (Exception e){
            logger.error("Feil i slettHighscore : " + e);
            return false;
        }
    }

    public List<LandOgBy> hentLandOgBy(){
        String sql = "SELECT * FROM LandOgBy ORDER BY land ASC;";
        try{
            List<LandOgBy> landOgBy = db.query(sql, new BeanPropertyRowMapper<>(LandOgBy.class));
            return landOgBy;
        }catch (Exception e){
            logger.error("Feil i hentLandOgBy : " + e);
            return null;
        }

    }

    public boolean login(Login bruker){
        String sql = "SELECT * FROM Login WHERE brukernavn = ?;";

        List<Login> finnBruker = db.query(sql, new BeanPropertyRowMapper<>(Login.class), bruker.getBrukernavn());

        if(finnBruker.size() > 0){
            String kryptertLagretPassord = finnBruker.get(0).getPassord();
            String loginPassord = bruker.getPassord();
            if(sjekkPassord(loginPassord, kryptertLagretPassord)){
                return true;
            }
        }
        return false;
    }

    public boolean sjekkPassord(String loginPassord, String kryptertLagretPassord){
        //return loginPassord.matches(lagretPassord);
        return BCrypt.checkpw(loginPassord,kryptertLagretPassord);
    }

    public boolean nyBruker(Login nyBruker){
        String sql = "INSERT INTO Login (brukernavn, passord) VALUES (?,?);";

        String kryptertPassord = hashPassord(nyBruker.getPassord());

        try{
            db.update(sql,nyBruker.getBrukernavn(),kryptertPassord);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    private String hashPassord(String passord){
        return BCrypt.hashpw(passord, BCrypt.gensalt(10));
    }
}
