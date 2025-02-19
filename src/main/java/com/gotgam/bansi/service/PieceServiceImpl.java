package com.gotgam.bansi.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.gotgam.bansi.DAO.ThumbNail;
import com.gotgam.bansi.DTO.PieceDTO.PieceRequest;
import com.gotgam.bansi.model.Image;
import com.gotgam.bansi.model.Keyword;
import com.gotgam.bansi.model.OptionalKeyword;
import com.gotgam.bansi.model.Piece;
import com.gotgam.bansi.model.User;
import com.gotgam.bansi.model.WhoKeyword;
import com.gotgam.bansi.respository.KeywordRepository;
import com.gotgam.bansi.respository.OptionalKeywordRepository;
import com.gotgam.bansi.respository.PieceRepository;
import com.gotgam.bansi.respository.WhoKeywordRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PieceServiceImpl implements PieceService {
    private final PieceRepository pieceRepository;
    private final KeywordRepository keywordRepository;
    private final WhoKeywordRepository whoKeywordRepository;
    private final OptionalKeywordRepository opKeywordRepository;
    private final UserService userService;
    private final ImageService imageService;
    private final PlaceKeywordService placeKeywordService;
    private final PieceLikeService likeService;
    private final CommentService commentService;

    private final Integer RANDOM_PIECES = 6;

    @Override
    public Piece getPieceByPieceId(Long pieceId) {
        Piece piece = pieceRepository.findById(pieceId).orElseThrow(() -> new NotFoundException("wrong pieceId"));
        return piece;
    }

    @Override
    public List<Piece> findPieceByUserId(String userId){
        User user = userService.getUserFromId(userId);
        List<Piece> pieces = pieceRepository.findByUser(user);
        return pieces;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Piece updatePiece(Long pieceId, Piece piece) {
        Piece oPiece = pieceRepository.findById(pieceId).orElseThrow(() -> new NotFoundException("wrong pieceId"));
        oPiece.withContent(piece.getContent())
              .withLatitude(piece.getLatitude())
              .withLongitude(piece.getLongitude());
        return oPiece;
    }

    // TODO: user는 userservice에서 가져와서 해주자
    // 읽기는 다른 서비스에서 가져와서 해주는게 좋은것 같음
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Piece savePiece(PieceRequest pieceRequest, String userId){
        User user = userService.getUserFromId(userId);
        List<Keyword> keywords = keywordRepository.findAllById(pieceRequest.getKeywords());
        List<OptionalKeyword> opKeywords = new ArrayList<>();
        if(pieceRequest.getOptionalKeywords().size() != 0){
            log.info(pieceRequest.getOptionalKeywords().toString());
            opKeywords = opKeywordRepository.findAllById(pieceRequest.getOptionalKeywords());
        }
        List<WhoKeyword> whos = whoKeywordRepository.findAllById(pieceRequest.getWhos());
        List<Image> images = imageService.saveAllImages(pieceRequest.getImages());
        Piece piece = new Piece()
            .withUser(user)
            .withDate(pieceRequest.getDate())
            .withContent(pieceRequest.getContent())
            .withLatitude(pieceRequest.getLatitude())
            .withLongitude(pieceRequest.getLongitude())
            .withPlaceUrl(pieceRequest.getPlaceUrl())
            .withAddress(pieceRequest.getAddress())
            .withAddressDetail(pieceRequest.getAddressDetail());
        piece.setPlace(placeKeywordService.getOrCreate(pieceRequest.getPlace()));
        piece.setKeywords(keywords);
        piece.setOpKeywords(opKeywords);
        piece.setWhos(whos);
        piece.setImages(images);

        pieceRepository.save(piece.withCreatedAt(new Date()));
        return piece;
    }

    @Override
    public List<ThumbNail> findRandomPieces(){
        List<Long> candIds = pieceRepository.findIdAll();
        Random rand = new Random(System.currentTimeMillis());
        List<Long> ids = new ArrayList<>();
        for(int i = 0; i < RANDOM_PIECES; i++){
            ids.add(candIds.get(rand.nextInt(candIds.size())));
        }
        return pieceRepository.findAllThumbNailById(ids);
    }

    @Override
    public void deletePiece(Long pieceId){
        pieceRepository.deleteById(pieceId);
    }

    @Override
    public List<ThumbNail> findThumbnails(String userId){
        List<ThumbNail> thumbnails = pieceRepository.findThumbNailByUserId(userId);
        return thumbnails;
    }

    @Override
    public List<ThumbNail> findByKeywordId(Long keywordId){
            List<Piece> pieces = pieceRepository.findAllByKeywords_Id(keywordId);
            List<ThumbNail> thumbnails = new ArrayList<>();
            // for(Piece piece : pieces){
            //     thumbnails.add(new ThumbNail(
            //         piece.getPieceId(), 
            //         piece.getImages().stream()
            //             .filter(img -> img.getThumbnail().equals(true))
            //             .findFirst()
            //             .orElse(piece.getImages().get(0)).getEncoded(), 
            //         piece.getUser().getKakaoId()));
            // }
        return thumbnails;
    }

    @Override
    public List<Piece> findByPlace(String placeName){
        List<Piece> pieces = pieceRepository.findAllByPlace_Name(placeName);
        return pieces;
    }

    @Override
    public List<Piece> findByKeyword(Long keywordId){
        List<Piece> pieces = pieceRepository.findAllByKeywords_Id(keywordId);
        return pieces;
    }

    @Override
    public List<Piece> findByWho(Long whoId){
        List<Piece> pieces = pieceRepository.findAllByWhos_Id(whoId);
        return pieces;
    }

    @Override
    public Long likePiece(Long pieceId, String userId){
        User user = userService.getUserFromId(userId);
        Piece piece = pieceRepository.findById(pieceId).orElseThrow(() -> new NotFoundException("잘못된 조각 아이디"));
        Long likeCount = likeService.createPieceLike(piece, user);
        return likeCount;
    }

    @Override
    public Long dislikePiece(Long pieceId, String userId){
        User user = userService.getUserFromId(userId);
        Piece piece = pieceRepository.findById(pieceId).orElseThrow(() -> new NotFoundException("잘못된 조각 아이디"));
        Long likeCount = likeService.deletePieceLike(piece, user);
        return likeCount;
    }

    @Override
    public void test(){
        List<Long> keywordIds = new ArrayList<>();
        List<Long> opKeywordIds = new ArrayList<>();
        List<Long> whosIds = new ArrayList<>();
        List<String> placeNames = new ArrayList<>();
        pieceRepository.findByKeywordsOrOpKeywordsOrWhosOrPlace(keywordIds, opKeywordIds, whosIds, placeNames);
    }
}
