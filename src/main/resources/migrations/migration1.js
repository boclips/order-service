const checkForHexRegExp = new RegExp("^[0-9a-fA-F]{24}$");

function getVideo(videoId) {
    const fields = {
        _id: 1,
        title: 1,
        source: 1,
        "legacy.type": 1
    };

    if (checkForHexRegExp.test(videoId)) {
        return db
            .getSiblingDB("video-service-db")
            .getCollection("videos")
            .findOne({_id: ObjectId(videoId)}, fields);
    } else {
        return db
            .getSiblingDB("video-service-db")
            .getCollection("videos")
            .findOne({aliases: [videoId]}, fields);
    }
}

function lookupLegacyOrders(order) {
    return order.items.map(item => {
        const videoId = db
            .getCollection("legacy-orders")
            .findOne({"items.uuid": item.uuid}, {"items.$": 1}).items[0].asset_id;

        const video = getVideo(videoId);
        const newItem = Object.assign({}, item, {
            source: {
                contentPartner: {
                    referenceId: video.source.contentPartner._id.valueOf(),
                    name: video.source.contentPartner.name
                },
                videoReference: video.source.videoReference
            },
            video: {
                referenceId: video._id.valueOf(),
                title: video.title,
                type: video.legacy.type
            }
        });

        return newItem;
    });
}

db.getCollection("orders")
    .find({})
    .map(order => {
        const items = lookupLegacyOrders(order);
        return db.getCollection("orders").update(
            {
                _id: order["_id"]
            },
            {
                $set: {
                    items: items
                }
            }
        );
    });
