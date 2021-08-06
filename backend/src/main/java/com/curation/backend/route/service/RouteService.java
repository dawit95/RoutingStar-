package com.curation.backend.route.service;

import com.curation.backend.place.domain.Place;
import com.curation.backend.place.domain.PlaceRepository;
import com.curation.backend.place.dto.PlaceRequestDto;
import com.curation.backend.route.domain.Route;
import com.curation.backend.route.domain.RouteRepository;
import com.curation.backend.route.dto.RouteDetailResponseDto;
import com.curation.backend.route.dto.RouteListResponseDto;
import com.curation.backend.route.dto.RouteRequestDto;
import com.curation.backend.route.dto.RouteSearchRequestDto;
import com.curation.backend.route.exception.NoRouteException;
import com.curation.backend.tag.domain.RouteWhatTagRepository;
import com.curation.backend.tag.domain.RouteWithTagRepository;
import com.curation.backend.tag.service.TagService;
import com.curation.backend.user.domain.FollowerFollowing;
import com.curation.backend.user.domain.LikeRepository;
import com.curation.backend.user.domain.User;
import com.curation.backend.user.domain.UserRepository;
import com.curation.backend.user.exception.NoUserException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final RouteWhatTagRepository routeWhatTagRepository;
    private final RouteWithTagRepository routeWithTagRepository;

    private final TagService tagService;


    Logger logger = LoggerFactory.getLogger(RouteService.class);

    @Transactional
    public Long save(RouteRequestDto routeRequestDto, List<PlaceRequestDto> placesRequestDto, List<Long> whatTagIds, List<Long> withTagIds) throws Exception {

        Route route = routeRequestDto.toEntity();
        Optional<User> user = Optional.ofNullable(userRepository.findById(routeRequestDto.getId()).orElseThrow(() -> new NoUserException("존재하지 않는 사용자입니다.")));


        route.setUser(user.get());
        routeRepository.save(route);

        List<Place> routePlaces = placesRequestDto.stream().map(e -> e.toEntity()).collect(Collectors.toList());

        for(Place p : routePlaces) {
            p.setRoute(route);
        }
        placeRepository.saveAll(routePlaces);

        tagService.addWhatTag(whatTagIds, route);
        tagService.addWithTag(withTagIds, route);

        return route.getId();
    }

    @Transactional(readOnly = true)
    public List<RouteListResponseDto> followingRouteList(Long id) throws NoUserException {
        Optional<User> user = Optional.ofNullable(userRepository.findById(id).orElseThrow(() -> new NoUserException("존재하지 않는 사용자입니다.")));
        List<FollowerFollowing> followList = user.get().getFollowers();

        List<Long> list = followList.stream().map(e -> e.getFollowing().getId()).collect(Collectors.toList());
        list.add(id);

        return routeRepository.findByUserIdInOrderByModifiedAtDesc(list).stream().map(RouteListResponseDto::new).collect(Collectors.toList());
    }

    public RouteDetailResponseDto getDetail(Long id) throws NoRouteException {
        Optional<Route> route = Optional.ofNullable(routeRepository.findById(id).orElseThrow(() -> new NoRouteException("해당하는 루트가 없습니다.")));
        return new RouteDetailResponseDto(route.get());
    }

    public List<RouteListResponseDto> likeRouteList() {
        return routeRepository.findAllOrderByLikeCount().stream().map(RouteListResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    public Long modifyRoute(Long id, RouteRequestDto routeRequestDto, List<PlaceRequestDto> placesRequestDto, List<Long> whatTagIds, List<Long> withTagIds) throws NoRouteException {
        Optional<Route> route = Optional.ofNullable(routeRepository.findById(id).orElseThrow(() -> new NoRouteException("해당하는 루트가 없습니다.")));

        route.get().modify(routeRequestDto);

        //기존에 연결되어 있던 것들 모두 삭제 필요
        placeRepository.deleteAllByRouteId(id);
        tagService.deleteWhatTag(id);
        tagService.deleteWithTag(id);

        List<Place> routePlaces = placesRequestDto.stream().map(e -> e.toEntity()).collect(Collectors.toList());

        for(Place p : routePlaces) {
            p.setRoute(route.get());
        }

        placeRepository.saveAll(routePlaces);

        tagService.addWhatTag(whatTagIds, route.get());
        tagService.addWithTag(withTagIds, route.get());

        return id;
    }

    public void deleteRoute(Long id) throws NoRouteException {
        Optional<Route> route = Optional.ofNullable(routeRepository.findById(id).orElseThrow(() -> new NoRouteException("해당하는 루트가 없습니다.")));
        route.get().delete();
        routeRepository.save(route.get());
    }

    @Transactional(readOnly = true)
    public List<RouteListResponseDto> myRouteList(Long id) {
        return routeRepository.findAllByUserId(id).stream().map(RouteListResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RouteListResponseDto> searchNonFollowingRoute(Long id, List<Long> whatTag, List<Long> withTag) throws NoUserException {
        Long whatTagSize = Long.valueOf(whatTag.size());
        Long withTagSize = Long.valueOf(withTag.size());

        List<Long> whatTagIdList = routeWhatTagRepository.findByWhatTag(whatTag, whatTagSize);
        List<Long> withTagIdList = routeWithTagRepository.findByWithTag(withTag, withTagSize);

        List<Long> routeIds = getRouteIds(whatTagSize, withTagSize, whatTagIdList, withTagIdList);

        logger.trace(String.valueOf(whatTagIdList) + "is distinct route!!!");
        logger.trace(String.valueOf(whatTagIdList) + "is distinct route about with!!!");

        Optional<User> user = Optional.ofNullable(userRepository.findById(id).orElseThrow(() -> new NoUserException("존재하지 않는 사용자입니다.")));
        List<FollowerFollowing> followList = user.get().getFollowers();

        List<Long> followerList = followList.stream().map(e -> e.getFollowing().getId()).collect(Collectors.toList());
        followerList.add(id);

        List<RouteListResponseDto> list = routeRepository.findByIdInAndUserIdNotIn(routeIds, followerList).stream().map(RouteListResponseDto::new).collect(Collectors.toList());

        return list;
    }

    @Transactional(readOnly = true)
    public List<RouteListResponseDto> searchFollowingRoute(Long id, List<Long> whatTag, List<Long> withTag) throws NoUserException {
        Long whatTagSize = Long.valueOf(whatTag.size());
        Long withTagSize = Long.valueOf(withTag.size());

        List<Long> whatTagIdList = routeWhatTagRepository.findByWhatTag(whatTag, whatTagSize);
        List<Long> withTagIdList = routeWithTagRepository.findByWithTag(withTag, withTagSize);

        List<Long> routeIds = getRouteIds(whatTagSize, withTagSize, whatTagIdList, withTagIdList);

        logger.trace(String.valueOf(whatTagIdList) + "is distinct route about what!!!");
        logger.trace(String.valueOf(withTagIdList) + "is distinct route about with!!!");

        Optional<User> user = Optional.ofNullable(userRepository.findById(id).orElseThrow(() -> new NoUserException("존재하지 않는 사용자입니다.")));
        List<FollowerFollowing> followList = user.get().getFollowers();

        List<Long> followerList = followList.stream().map(e -> e.getFollowing().getId()).collect(Collectors.toList());
        followerList.add(id);

        List<RouteListResponseDto> list = routeRepository.findByIdInAndUserIdIn(routeIds, followerList).stream().map(RouteListResponseDto::new).collect(Collectors.toList());

        return list;
    }

    private List<Long> getRouteIds(Long whatTagSize, Long withTagSize, List<Long> whatTagIdList, List<Long> withTagIdList) {
        List<Long> routeIds = new ArrayList<Long>();

        if (whatTagSize != 0 && withTagSize != 0) {
            routeIds.addAll(whatTagIdList);
            routeIds.retainAll(withTagIdList);
        } else if (whatTagSize != 0 && withTagSize == 0) {
            routeIds.addAll(whatTagIdList);
        } else if (whatTagSize == 0 && withTagSize != 0) {
            routeIds.addAll(withTagIdList);
        }
        return routeIds;
    }
}


