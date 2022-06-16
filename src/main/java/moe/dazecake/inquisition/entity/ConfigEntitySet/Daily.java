package moe.dazecake.inquisition.entity.ConfigEntitySet;

import lombok.Data;

import java.util.List;

@Data
public class Daily {

    private List<Fight> fight;
    private Sanity sanity;
    private boolean mail;
    private Offer offer;
    private boolean friends;
    private Infrastructure infrastructure;
    private boolean credit;
    private boolean task;
    private boolean activity;

}